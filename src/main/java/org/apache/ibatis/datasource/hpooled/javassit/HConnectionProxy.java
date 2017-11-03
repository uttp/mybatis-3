package org.apache.ibatis.datasource.hpooled.javassit;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

/**
 * Created by zhangyehui on 2017/11/2.
 */
public abstract class HConnectionProxy implements Connection {
    private List<Statement> currentStatements;


}
