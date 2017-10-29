package org.apache.ibatis.datasource.hpooled;

import org.apache.ibatis.exceptions.ConnectionCloseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static java.lang.Thread.yield;

/**
 * Created by zhangyehui on 2017/10/25.
 */
public class HConnectionPooled {
    /**
     * 连接池
     */
    private CopyOnWriteArrayList<HConnectionEntry> pooledList;

    private HDataSourceConfig hDataSourceConfig;

    /**
     * 连接的状态, 无锁化的基础
     */
    private AtomicIntegerFieldUpdater<HConnectionEntry> entryUpdater = AtomicIntegerFieldUpdater.newUpdater(HConnectionEntry.class, "state");

    /**
     * 用户获取连接
     */
    private SynchronousQueue<HConnectionEntry> queue;

    /**
     * 创建Connection阻塞队列
     */
    private BlockingQueue<Runnable> blockingQueue;

    /**
     * 创建线程的线程池
     */
    private ThreadPoolExecutor createPoolExecutor;

    /**
     * 等待获取连接的数目
     */
    private AtomicInteger waiters;

    /**
     * 定时清理过期的连接
     */
    private ScheduledExecutorService scheduleCleanIdleExecutorService;

    private RecycleConnectionService RECYCLE_CONNECTION_SERVICE = new RecycleConnectionService(this);

    public HConnectionPooled(HDataSourceConfig hDataSourceConfig) {
        this.hDataSourceConfig = hDataSourceConfig;
        init();
    }

    private void init() {
        pooledList = new CopyOnWriteArrayList<>();

        queue = new SynchronousQueue<>();
        waiters = new AtomicInteger();
        scheduleCleanIdleExecutorService = new ScheduledThreadPoolExecutor(1);
        scheduleCleanIdleExecutorService.scheduleAtFixedRate(RECYCLE_CONNECTION_SERVICE, 500, 10_000, TimeUnit.MILLISECONDS);
        createPoolExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, blockingQueue);
        int poolSize = hDataSourceConfig.getPoolSize();
        blockingQueue = new LinkedBlockingQueue<>(poolSize);
    }

    public Connection fetchConnection() {
        long connectionTimeOut = hDataSourceConfig.getConnectionTimeOut(), timeOut = connectionTimeOut;
        try {
            waiters.incrementAndGet();

            do {
                long startTime = System.currentTimeMillis();
                for (HConnectionEntry connectionEntry : pooledList) {
                    if (entryUpdater.compareAndSet(connectionEntry, HConnectionState.NOT_IN_USED.getValue(),
                            HConnectionState.IN_USED.getValue())) {
                        if (waiters.get() > 1) {
                            createNewConnection();
                        }
                        return connectionEntry;
                    }
                }

                createNewConnection();

                HConnectionEntry connectionEntry = queue.poll(connectionTimeOut, TimeUnit.MILLISECONDS);
                if (entryUpdater.compareAndSet(connectionEntry, HConnectionState.NOT_IN_USED.getValue(),
                        HConnectionState.IN_USED.getValue())) {
                    return connectionEntry;
                }

                timeOut = timeOut - (System.currentTimeMillis() - startTime);
            } while (timeOut > 0L);


        } catch (InterruptedException e) {

        } finally {
            waiters.decrementAndGet();
        }


        return null;
    }

    public void createNewConnection() {
        createPoolExecutor.execute(new ConnectionCreated(this));
    }

    //创建Connection比较耗时
    public void createConnection() throws SQLException {
        Connection rawConnection = DriverManager.getConnection(hDataSourceConfig.getUrl(), hDataSourceConfig.getUserName(), hDataSourceConfig.getPassword());
        HConnectionEntry hConnectionEntry = new HConnectionEntry(rawConnection, this);
        pooledList.add(hConnectionEntry);

        if (waiters.get() > 0 && queue.offer(hConnectionEntry)) {
            yield();
        }
    }

    //关闭Connection，对Connection资源进行回收
    public void recycleConnection(HConnectionEntry hConnectionEntry) {
        entryUpdater.set(hConnectionEntry, HConnectionState.NOT_IN_USED.getValue());
        hConnectionEntry.setStartIdleTime(System.currentTimeMillis());
    }

    public void closeConnection(HConnectionEntry hConnectionEntry) {
        if (entryUpdater.compareAndSet(hConnectionEntry, HConnectionState.NOT_IN_USED.getValue(), HConnectionState.IN_RECYCLE.getValue())) {
            pooledList.remove(hConnectionEntry);
            try {
                hConnectionEntry.getDelegate().close();
            } catch (SQLException e) {
                throw new ConnectionCloseException("close failed", e);
            }
        }

    }

    public CopyOnWriteArrayList<HConnectionEntry> getPooledList() {
        return pooledList;
    }

    public HDataSourceConfig gethDataSourceConfig() {
        return hDataSourceConfig;
    }

}
