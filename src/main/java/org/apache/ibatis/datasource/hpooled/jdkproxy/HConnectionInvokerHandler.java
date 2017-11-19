package org.apache.ibatis.datasource.hpooled.jdkproxy;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
        if (METHOD_NAME_CREATE_STATEMENT.equals(method.getName())) {
            Statement realStatement = (Statement) method.invoke(delegate, args);
            Statement delegateStatement = (Statement) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class[]{Statement.class}, new HStatementInvokerHandler(realStatement, statements));
            statements.add(realStatement);
            return delegateStatement;
        }

        if (METHOD_NAME_PREPARE_STATEMENT.equals(method.getName())) {
            PreparedStatement realStatement = (PreparedStatement) method.invoke(delegate, args);
            PreparedStatement delegateStatement = (PreparedStatement) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class[]{PreparedStatement.class}, new HStatementInvokerHandler(realStatement, statements));
            statements.add(delegateStatement);
            return delegateStatement;
        }

        if (METHOD_NAME_PREPARE_CALL.equals(method.getName())) {
            CallableStatement realStatement = (CallableStatement) method.invoke(delegate, args);
            CallableStatement delegateStatement = (CallableStatement) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class[]{CallableStatement.class}, new HStatementInvokerHandler(realStatement, statements));
            statements.add(delegateStatement);
            return delegateStatement;
        }

        if (METHOD_NAME_PREPARE_STATEMENT.equals(method.getName())) {
            PreparedStatement realStatment = (PreparedStatement) method.invoke(delegate, args);
            PreparedStatement delegateStatement = (PreparedStatement) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class[]{PreparedStatement.class}, new HPrepareStatementInvokerHandler(realStatment, statements));
            return delegateStatement;
        }

        if (METHOD_NAME_CREATE_STATEMENT.equals(method.getName())) {
            CallableStatement realStatement = (CallableStatement) method.invoke(delegate, args);
            CallableStatement delegateStatement = (CallableStatement) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                    new Class[]{CallableStatement.class}, new HCallableStatementInvokerHandler(realStatement, statements));
            return delegateStatement;
        }

        if (HStatementConstants.CLOSE.equals(method.getName())) {
            hConnectionEntry.recycleConnection();
        }

        return method.invoke(delegate, args);

    }
}
