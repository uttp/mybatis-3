package org.apache.ibatis.datasource.hpooled;

/**
 * Created by zhangyehui on 2017/10/25.
 */
public class HDataSourceConfig {
    private String url;
    private String userName;
    private String password;
    private String driverName;

    private int poolSize;
    private long connectionTimeOut;
    private long longestLiveTime;
    private long longestIdleTime;

    public HDataSourceConfig() {

    }

    public HDataSourceConfig(String url, String userName, String password) {
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getConnectionTimeOut() {
        return connectionTimeOut;
    }

    public void setConnectionTimeOut(long connectionTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public long getLongestLiveTime() {
        return longestLiveTime;
    }

    public void setLongestLiveTime(long longestLiveTime) {
        this.longestLiveTime = longestLiveTime;
    }

    public long getLongestIdleTime() {
        return longestIdleTime;
    }

    public void setLongestIdleTime(long longestIdleTime) {
        this.longestIdleTime = longestIdleTime;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
}
