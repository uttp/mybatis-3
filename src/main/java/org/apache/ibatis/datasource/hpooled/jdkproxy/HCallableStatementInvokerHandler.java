package org.apache.ibatis.datasource.hpooled.jdkproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Statement;
import java.util.List;

/**
 * Created by zhangyehui on 2017/11/12.
 */
public class HCallableStatementInvokerHandler implements InvocationHandler {
    private CallableStatement realStatement;
    private List<Statement> statements;

    public HCallableStatementInvokerHandler(CallableStatement realStatement, List<Statement> statements) {
        this.realStatement = realStatement;
        this.statements = statements;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (HStatementConstants.CLOSE.equals(method.getName())) {
            statements.remove(realStatement);
        }

        return method.invoke(realStatement, args);
    }
}
