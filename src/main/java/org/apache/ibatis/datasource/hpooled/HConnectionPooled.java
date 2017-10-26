package org.apache.ibatis.datasource.hpooled;

import org.apache.ibatis.exceptions.IbatisException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static java.lang.Thread.yield;

/**
 * Created by zhangyehui on 2017/10/25.
 */
public class HConnectionPooled {
    private CopyOnWriteArrayList<HConnectionEntry> pooledList;
    private DataSourceConfig dataSourceConfig;
    private HPooledDataSource hPooledDataSource;
    private AtomicIntegerFieldUpdater<HConnectionEntry> entryUpdater = AtomicIntegerFieldUpdater.newUpdater(HConnectionEntry.class, "state");
    private SynchronousQueue<HConnectionEntry> queue;
    private AtomicInteger waiters;

    public HConnectionPooled(HPooledDataSource hPooledDataSource) {
        this.hPooledDataSource = hPooledDataSource;
        this.dataSourceConfig = hPooledDataSource.getDataSourceConfig();
        init();
    }

    private void init() {
        pooledList = new CopyOnWriteArrayList<>();
        try {
            Class.forName(dataSourceConfig.getDriverName());
        } catch (ClassNotFoundException classNotFoundException) {
            throw new IbatisException("find driver class failed!");
        }

        queue = new SynchronousQueue<>();
        waiters = new AtomicInteger();
    }

    public Connection fetchConnection() {
        long connectionTimeOut = dataSourceConfig.getConnectionTimeOut(), timeOut = connectionTimeOut;
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
        Connection rawConnection = DriverManager.getConnection(dataSourceConfig.getUrl(), dataSourceConfig.getUserName(), dataSourceConfig.getPassword());
        HConnectionEntry hConnectionEntry = new HConnectionEntry(rawConnection);
        pooledList.add(hConnectionEntry);

        if (waiters.get() > 0 && queue.offer(hConnectionEntry)) {
            yield();
        }
    }

}
