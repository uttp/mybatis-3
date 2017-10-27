package org.apache.ibatis.datasource.hpooled;

/**
 * Created by zhangyehui on 2017/10/27.
 */

import org.junit.Test;

import java.sql.*;

public class HPooledDataSourceTest {

    @Test
    public void testConnection() throws SQLException {
        HDataSourceConfig hDataSourceConfig = initDataSourceConfig();
        HPooledDataSource hPooledDataSource = new HPooledDataSource(hDataSourceConfig);
        Connection connection = hPooledDataSource.getConnection();
        String sql = "select * from book where book_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, 4);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            Integer id = resultSet.getInt(1);
            System.out.println(id);
        }
    }

    private HDataSourceConfig initDataSourceConfig() {
        HDataSourceConfig hDataSourceConfig = new HDataSourceConfig();
        hDataSourceConfig.setDriverName("com.mysql.jdbc.Driver");
        hDataSourceConfig.setUrl("jdbc:mysql://127.0.0.1:3306/test");
        hDataSourceConfig.setUserName("root");
        hDataSourceConfig.setPassword("123qwe");
        hDataSourceConfig.setPoolSize(5);
        hDataSourceConfig.setConnectionTimeOut(10_000);
        hDataSourceConfig.setLongestIdleTime(60_000);
        hDataSourceConfig.setLongestLiveTime(300_000);
        return hDataSourceConfig;
    }
}
