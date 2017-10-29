package org.apache.ibatis.datasource.hpooled;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Created by zhangyehui on 2017/10/25.
 */
public class HPooledDataSource implements DataSource {
    private HDataSourceConfig hDataSourceConfig;
    private HConnectionPooled hConnectionPooled;

    public HPooledDataSource(HDataSourceConfig hDataSourceConfig) {
        this.hDataSourceConfig = hDataSourceConfig;
        hConnectionPooled = new HConnectionPooled(hDataSourceConfig);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = hConnectionPooled.fetchConnection();
        if (null == connection) {
            throw new SQLException("connection timeout");
        }
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
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

    public HDataSourceConfig gethDataSourceConfig() {
        return hDataSourceConfig;
    }

    public void sethDataSourceConfig(HDataSourceConfig hDataSourceConfig) {
        this.hDataSourceConfig = hDataSourceConfig;
    }

    public HConnectionPooled gethConnectionPooled() {
        return hConnectionPooled;
    }

}
