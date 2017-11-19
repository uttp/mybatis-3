package org.apache.ibatis.datasource.hpooled.jdkproxy;

import org.apache.ibatis.datasource.hpooled.HConnectionPooled;
import org.apache.ibatis.datasource.hpooled.HConnectionState;
import org.apache.ibatis.datasource.hpooled.exception.CloseStatementException;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
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
    private Connection delegateConnection;
    private Connection realConnection;
    private List<Statement> statements;
    private long idleStartTime;

    public HConnectionEntry(HConnectionPooled hConnectionPooled) {
        this.hConnectionPooled = hConnectionPooled;
        init();
    }

    private void init() {
        state = HConnectionState.NOT_IN_USED.getValue();
        realConnection = hConnectionPooled.newConnection();
        statements = new ArrayList<>();
        idleStartTime = System.currentTimeMillis();
        delegateConnection = (Connection) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Connection.class},
                new HConnectionInvokerHandler(this, realConnection, statements));
    }

    public Connection getConnection() {
        return delegateConnection;
    }

    public synchronized void recycleConnection() {
        if (atomicState.compareAndSet(this, HConnectionState.IN_USED.getValue(), HConnectionState.NOT_IN_USED.getValue())) {
            idleStartTime = System.currentTimeMillis();
            closeStatements();
        }
    }

    public boolean compareAndSet(int expectValue, int newValue) {
        return atomicState.compareAndSet(this, expectValue, newValue);
    }

    public void close() {
        atomicState.compareAndSet(this, HConnectionState.NOT_IN_USED.getValue(), HConnectionState.IN_RECYCLE.getValue());
        try {
            if (!realConnection.isClosed()) {
                realConnection.close();
            }
        } catch (SQLException e) {
            throw new CloseStatementException(e);
        }
    }

    private void closeStatements() {
        for (Statement statement : statements) {
            try {
                if (!statement.isClosed()) {
                    statement.close();
                }
            } catch (SQLException e) {
                throw new CloseStatementException(e);
            }
        }
    }

    public long getIdleStartTime() {
        return idleStartTime;
    }

    public void setIdleStartTime(long idleStartTime) {
        this.idleStartTime = idleStartTime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Connection getRealConnection() {
        return realConnection;
    }

    public void setRealConnection(Connection realConnection) {
        this.realConnection = realConnection;
    }
}
