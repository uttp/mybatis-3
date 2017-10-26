package org.apache.ibatis.datasource.hpooled;

/**
 * Created by zhangyehui on 2017/10/25.
 */
public class HConnectionState {
    public static final int NOT_IN_USED = 0;
    public static final int IN_USED = 1;
    public static final int IN_RECYCLE = 2;
}
