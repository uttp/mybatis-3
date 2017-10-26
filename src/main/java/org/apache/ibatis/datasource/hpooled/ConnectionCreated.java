package org.apache.ibatis.datasource.hpooled;

import java.sql.SQLException;

/**
 * Created by zhangyehui on 2017/10/26.
 */
public class ConnectionCreated implements Runnable {
    private HConnectionPooled hConnectionPooled;

    public ConnectionCreated(HConnectionPooled hConnectionPooled) {
        this.hConnectionPooled = hConnectionPooled;
    }

    @Override
    public void run() {
        try {
            hConnectionPooled.createConnection();
        } catch (SQLException e) {
            System.out.println("create connection failed!");
        }

    }
}
