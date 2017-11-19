package org.apache.ibatis.datasource.hpooled;

import org.apache.ibatis.datasource.hpooled.exception.CreateConnectionException;
import org.apache.ibatis.datasource.hpooled.jdkproxy.HConnectionEntry;
import org.apache.ibatis.exceptions.ConnectionCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.yield;

/**
 * Created by zhangyehui on 2017/10/25.
 */
public class HConnectionPooled {
    private static Logger LOGGER = LoggerFactory.getLogger(HConnectionPooled.class);

    /**
     * 连接池
     */
    private CopyOnWriteArrayList<HConnectionEntry> pooledList;

    private HDataSourceConfig hDataSourceConfig;

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

    private CloseConnectionService RECYCLE_CONNECTION_SERVICE = new CloseConnectionService(this);

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
            int waiterCount = waiters.incrementAndGet();
            do {
                long startTime = System.currentTimeMillis();
                for (HConnectionEntry connectionEntry : pooledList) {
                    if (connectionEntry.compareAndSet(HConnectionState.NOT_IN_USED.getValue(), HConnectionState.IN_USED.getValue())) {
                        if (waiterCount > 1) {
                            createNewConnection();
                        }
                        return connectionEntry.getConnection();
                    }
                }

                createNewConnection();

                HConnectionEntry connectionEntry = queue.poll(connectionTimeOut, TimeUnit.MILLISECONDS);
                if (connectionEntry.compareAndSet(HConnectionState.NOT_IN_USED.getValue(),
                        HConnectionState.IN_USED.getValue())) {
                    return connectionEntry.getConnection();
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
        createPoolExecutor.execute(() -> {
            try {
                this.createConnection();
            } catch (SQLException e) {
                LOGGER.error("create connection failed ", e);
            }
        });
    }

    /**
     * 不会存在并发问题，线程池会进行排队
     *
     * @throws SQLException
     */
    public void createConnection() throws SQLException {
        if (pooledList.size() + blockingQueue.size() < hDataSourceConfig.getPoolSize()) {
            HConnectionEntry hConnectionEntry = new HConnectionEntry(this);
            pooledList.add(hConnectionEntry);
            if (waiters.get() > 0 && queue.offer(hConnectionEntry)) {
                yield();
            }
        }
    }

    public void closeConnection(HConnectionEntry hConnectionEntry) {
        if (hConnectionEntry.compareAndSet(HConnectionState.NOT_IN_USED.getValue(), HConnectionState.IN_RECYCLE.getValue())) {
            pooledList.remove(hConnectionEntry);
            try {
                Connection delegate = hConnectionEntry.getConnection();
                if (!delegate.isClosed()) {
                    delegate.close();
                }
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

    public Connection newConnection() throws CreateConnectionException {
        try {
            Connection connection = DriverManager.getConnection(hDataSourceConfig.getUrl(),
                    hDataSourceConfig.getUserName(), hDataSourceConfig.getPassword());
            return connection;
        } catch (SQLException e) {
            throw new CreateConnectionException(e);
        }
    }

}
