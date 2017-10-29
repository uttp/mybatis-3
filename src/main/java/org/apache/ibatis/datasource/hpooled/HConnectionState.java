package org.apache.ibatis.datasource.hpooled;

/**
 * Created by zhangyehui on 2017/10/25.
 */
public enum HConnectionState {
    NOT_IN_USED(0),
    IN_USED(1),
    IN_RECYCLE(2);

    private int value;

    HConnectionState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
