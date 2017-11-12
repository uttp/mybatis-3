package org.apache.ibatis.datasource.hpooled.jdkproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.List;

/**
 * Created by zhangyehui on 2017/11/11.
 */
public class HStatementInvokerHandler implements InvocationHandler {
    private List<Statement> statements;
    private Statement realStatement;

    public HStatementInvokerHandler(Statement realStatement, List<Statement> statements) {
        this.statements = statements;
        this.realStatement = realStatement;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (HStatementConstants.CLOSE.equals(method.getName())) {
            statements.remove(realStatement);
        }

        return method.invoke(realStatement, args);
    }
}
