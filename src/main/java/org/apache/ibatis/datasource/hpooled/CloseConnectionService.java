package org.apache.ibatis.datasource.hpooled;


import org.apache.ibatis.datasource.hpooled.jdkproxy.HConnectionEntry;

import java.util.List;

/**
 * Created by zhangyehui on 2017/10/26.
 */
public class CloseConnectionService implements Runnable {
    private HConnectionPooled hConnectionPooled;

    public CloseConnectionService(HConnectionPooled hConnectionPooled) {
        this.hConnectionPooled = hConnectionPooled;
    }

    @Override
    public void run() {
        List<HConnectionEntry> pooledList = hConnectionPooled.getPooledList();
        long idleTimeOut = hConnectionPooled.gethDataSourceConfig().getLongestIdleTime();
        for (HConnectionEntry hConnectionEntry : pooledList) {
            if (hConnectionEntry.getState() == HConnectionState.NOT_IN_USED.getValue()
                    && (System.currentTimeMillis() - hConnectionEntry.getIdleStartTime()) > idleTimeOut) {
                hConnectionPooled.closeConnection(hConnectionEntry);
            }
        }

    }
}
