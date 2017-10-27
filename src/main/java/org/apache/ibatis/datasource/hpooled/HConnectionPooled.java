package org.apache.ibatis.datasource.hpooled;

import org.apache.ibatis.exceptions.IbatisException;

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
    private CopyOnWriteArrayList<HConnectionEntry> pooledList;
    private HDataSourceConfig hDataSourceConfig;
    private HPooledDataSource hPooledDataSource;
    private AtomicIntegerFieldUpdater<HConnectionEntry> entryUpdater = AtomicIntegerFieldUpdater.newUpdater(HConnectionEntry.class, "state");
    private SynchronousQueue<HConnectionEntry> queue;
    private AtomicInteger waiters;
    private ScheduledExecutorService scheduledExecutorService;
    private RecycleConnectionService RECYCLE_CONNECTION_SERVICE;

    public HConnectionPooled(HPooledDataSource hPooledDataSource) {
        this.hPooledDataSource = hPooledDataSource;
        this.hDataSourceConfig = hPooledDataSource.getHDataSourceConfig();
        init();
    }

    private void init() {
        pooledList = new CopyOnWriteArrayList<>();
        try {
            Class.forName(hDataSourceConfig.getDriverName());
        } catch (ClassNotFoundException classNotFoundException) {
            throw new IbatisException("find driver class failed!");
        }

        queue = new SynchronousQueue<>();
        waiters = new AtomicInteger();
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1);
        RECYCLE_CONNECTION_SERVICE = new RecycleConnectionService(this);
        scheduledExecutorService.scheduleAtFixedRate(RECYCLE_CONNECTION_SERVICE, 500, 30_000, TimeUnit.MILLISECONDS);

    }

    public Connection fetchConnection() {
        long connectionTimeOut = hDataSourceConfig.getConnectionTimeOut(), timeOut = connectionTimeOut;
        try {
            waiters.incrementAndGet();

            do {
                long startTime = System.currentTimeMillis();
                for (HConnectionEntry connectionEntry : pooledList) {
                    if (entryUpdater.compareAndSet(connectionEntry, HConnectionState.NOT_IN_USED, HConnectionState.IN_USED)) {
                        if (waiters.get() > 1) {
                            hPooledDataSource.createConnection();
                        }
                        return connectionEntry;
                    }
                }

                hPooledDataSource.createConnection();

                HConnectionEntry connectionEntry = queue.poll(connectionTimeOut, TimeUnit.MILLISECONDS);
                if (entryUpdater.compareAndSet(connectionEntry, HConnectionState.NOT_IN_USED, HConnectionState.IN_USED)) {
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
        entryUpdater.set(hConnectionEntry, HConnectionState.NOT_IN_USED);
        hConnectionEntry.setStartIdleTime(System.currentTimeMillis());
    }

    public void closeConnection(HConnectionEntry hConnectionEntry) {
        if (entryUpdater.compareAndSet(hConnectionEntry, HConnectionState.NOT_IN_USED, HConnectionState.IN_RECYCLE)) {
            pooledList.remove(hConnectionEntry);

            try {
                hConnectionEntry.getDelegate().close();
            } catch (SQLException e) {
                e.printStackTrace();
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
