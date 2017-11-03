package org.apache.ibatis.datasource.hpooled.jdkproxy;


import net.sf.cglib.proxy.InvocationHandler;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 使用jdk动态代理
 * Created by zhangyehui on 2017/11/3.
 */
public class HConnectionInvokerHandler implements InvocationHandler {
    private static final String METHOD_NAME_CREATE_STATEMENT = "createStatement";
    private static final String METHOD_NAME_PREPARE_STATEMENT = "prepareStatement";
    private static final String METHOD_NAME_PREPARE_CALL = "prepareCall";
    private static final String METHOD_NAME_CLOSE = "close";

    private HConnectionEntry hConnectionEntry;
    private List<Statement> statements;
    private Connection delegate;

    public HConnectionInvokerHandler(HConnectionEntry hConnectionEntry, Connection delegate, List<Statement> statements) {
        this.hConnectionEntry = hConnectionEntry;
        this.statements = new ArrayList<>();
        this.delegate = delegate;
        this.statements = statements;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        /**
         * Collection 回收到连接池时候需要关闭statement
         */
        if (METHOD_NAME_CREATE_STATEMENT.equals(method.getName()) ||
                METHOD_NAME_PREPARE_STATEMENT.equals(method.getName()) ||
                METHOD_NAME_PREPARE_CALL.equals(method.getName())) {
            Statement statement = (Statement) method.invoke(delegate, args);
            statements.add(statement);
            return statement;
        }

        if (METHOD_NAME_CLOSE.equals(method.getName())) {

        }

        return method.invoke(delegate, args);

    }
}
