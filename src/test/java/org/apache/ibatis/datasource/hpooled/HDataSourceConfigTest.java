package org.apache.ibatis.datasource.hpooled;

import org.junit.Test;

/**
 * Created by zhangyehui on 2017/10/28.
 */
public class HDataSourceConfigTest {

    @Test
    public void testDriver() {
        HDataSourceConfig hDataSourceConfig = new HDataSourceConfig("jdbc:mysql://127.0.0.1:3306/test", "root", "123qwe");
        hDataSourceConfig.setDriverName("com.mysql.jdbc.Driver");
        hDataSourceConfig.initConfig();
    }
}
