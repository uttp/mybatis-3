package org.apache.ibatis.datasource.hpooled;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Created by zhangyehui on 2017/10/25.
 */
public class HPooledDataSource implements DataSource {
    private DataSourceConfig dataSourceConfig;
    private HConnectionPooled hConnectionPooled;
    private BlockingQueue<Runnable> blockingQueue;
    private ThreadPoolExecutor createPoolExecutor;

    public HPooledDataSource(DataSourceConfig dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
        init();
    }

    private void init() {
        hConnectionPooled = new HConnectionPooled(this);
        int poolSize = dataSourceConfig.getPoolSize();
        blockingQueue = new LinkedBlockingQueue<>(poolSize);
        createPoolExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, blockingQueue);
    }

    public void createConnection() {
        createPoolExecutor.execute(new ConnectionCreated(hConnectionPooled));
    }


    @Override
    public Connection getConnection() throws SQLException {

        return null;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    public DataSourceConfig getDataSourceConfig() {
        return dataSourceConfig;
    }

    public void setDataSourceConfig(DataSourceConfig dataSourceConfig) {
        this.dataSourceConfig = dataSourceConfig;
    }
}
