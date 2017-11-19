package org.apache.ibatis.datasource.hpooled;

/**
 * Created by zhangyehui on 2017/10/27.
 */

import org.apache.ibatis.datasource.hpooled.jdkproxy.HConnectionEntry;
import org.junit.Test;

import java.sql.*;
import java.util.concurrent.CopyOnWriteArrayList;

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
        connection.close();
    }

    @Test
    public void testPooledSize() throws SQLException {
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
        CopyOnWriteArrayList<HConnectionEntry> pooled = hPooledDataSource.gethConnectionPooled().getPooledList();
        System.out.println("-------size : " + pooled.size() + "--------");
        for (int i = 0; i < pooled.size(); ++i) {
            HConnectionEntry hConnectionEntry = pooled.get(i);
            System.out.println("index :" + i + " state:" + hConnectionEntry.getState() + " real connection isClosed:" + hConnectionEntry.getRealConnection().isClosed());
        }
        connection.close();
        try {
            Thread.sleep(30_000);
        } catch (InterruptedException e) {

        }
        System.out.println("-------size :  " + pooled.size() + "--------");
        for (int i = 0; i < pooled.size(); ++i) {
            HConnectionEntry hConnectionEntry = pooled.get(i);
            System.out.println("index :" + i + " state:" + hConnectionEntry.getState() + " real connection isClosed:" + hConnectionEntry.getRealConnection().isClosed());
        }
        connection.close();
    }

    private HDataSourceConfig initDataSourceConfig() {
        HDataSourceConfig hDataSourceConfig = new HDataSourceConfig();
        hDataSourceConfig.setDriverName("com.mysql.jdbc.Driver");
        hDataSourceConfig.setUrl("jdbc:mysql://127.0.0.1:3306/test");
        hDataSourceConfig.setUserName("root");
        hDataSourceConfig.setPassword("123qwe");
        hDataSourceConfig.setPoolSize(5);
        hDataSourceConfig.setConnectionTimeOut(10_000);
        hDataSourceConfig.setLongestIdleTime(20_000);
        hDataSourceConfig.setLongestLiveTime(300_000);
        return hDataSourceConfig;
    }
}
