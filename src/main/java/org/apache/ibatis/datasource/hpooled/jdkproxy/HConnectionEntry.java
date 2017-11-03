package org.apache.ibatis.datasource.hpooled.jdkproxy;

import net.sf.cglib.proxy.Proxy;
import org.apache.ibatis.datasource.hpooled.HConnectionPooled;
import org.apache.ibatis.datasource.hpooled.HConnectionState;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Created by zhangyehui on 2017/11/3.
 */
public class HConnectionEntry {
    private static AtomicIntegerFieldUpdater<HConnectionEntry> atomicState = AtomicIntegerFieldUpdater.newUpdater(HConnectionEntry.class, "state");
    private volatile int state;

    private HConnectionPooled hConnectionPooled;
    private Connection deleagteConnection;
    private Connection realConnection;
    private List<Statement> statements;

    public HConnectionEntry(HConnectionPooled hConnectionPooled) {
        this.hConnectionPooled = hConnectionPooled;
        init();
    }

    private void init() {
        state = HConnectionState.NOT_IN_USED.getValue();
        realConnection = hConnectionPooled.newConnection();
        statements = new ArrayList<>();
        deleagteConnection = (Connection) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Connection.class},
                new HConnectionInvokerHandler(this, realConnection, statements));
    }

    private void recycleConnection() {

    }

    private void closeStatements() {
        for (Statement statement : statements) {
        }
    }


}
