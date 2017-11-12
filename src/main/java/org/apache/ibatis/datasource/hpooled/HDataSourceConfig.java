package org.apache.ibatis.datasource.hpooled;

import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangyehui on 2017/10/25.
 */
public class HDataSourceConfig {
    private String url;
    private String userName;
    private String password;
    private String driverName;

    private static int DEFAULT_POOL_SIZE = 5;
    private static long DEFAULT_CONNECTION_TIME_OUT = 5_000;
    private static long DEFAULT_LONGEST_LIVE_TIME = 600_000;
    private static long DEFAULT_LONGEST_IDLE_TIME = 10_000;

    /**
     * DataSource池大小
     */
    private int poolSize;

    /**
     * 获取连接超时时间
     */
    private long connectionTimeOut;

    /**
     * 连接最长存活时间
     */
    private long longestLiveTime;

    /**
     * 连接最长空闲时间
     */
    private long longestIdleTime;

    /**
     * 注册过的驱动
     */
    private static Map<String, Driver> driverMap = new HashMap<>();

    /**
     * 是否自动提交
     */
    private boolean isAutoCommit;

    /**
     * 是否只读
     */
    private boolean isReadOnly;

    static {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        if (null != drivers) {
            while (drivers.hasMoreElements()) {
                Driver driver = drivers.nextElement();
                driverMap.put(driver.getClass().getName(), driver);
            }
        }
    }

    public HDataSourceConfig() {

    }

    public HDataSourceConfig(String url, String userName, String password) {
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

    /**
     * 初始化
     */
    public void init() {
        initParams();
        initDriver();
    }

    /**
     * 初始化参数
     */
    private void initParams() {
        if (null == url || null == userName || null == password) {
            throw new IllegalArgumentException("url or userName or Password is null");
        }

        if (poolSize <= 0) {
            poolSize = DEFAULT_POOL_SIZE;
        }

        if (connectionTimeOut <= 0L) {
            connectionTimeOut = DEFAULT_CONNECTION_TIME_OUT;
        }

        if (longestLiveTime <= 0L) {
            longestLiveTime = DEFAULT_LONGEST_LIVE_TIME;
        }

        if (longestIdleTime <= 0L) {
            longestIdleTime = DEFAULT_LONGEST_IDLE_TIME;
        }
    }

    /**
     * 初始化驱动
     */
    private void initDriver() {
        Driver driver = driverMap.get(driverName);
        if (null == driver) {
            try {
                Class.forName(driverName);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("driverName class not found", e);
            }
        }
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

    public boolean isAutoCommit() {
        return isAutoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        isAutoCommit = autoCommit;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }
}
