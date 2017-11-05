package org.apache.ibatis.datasource.hpooled.exception;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * Created by zhangyehui on 2017/11/5.
 */
public class CloseStatementException extends PersistenceException {

    private static final long serialVersionUID = 4890764316503392116L;

    public CloseStatementException() {
        super();
    }

    public CloseStatementException(String message) {
        super(message);
    }

    public CloseStatementException(String message, Throwable cause) {
        super(message, cause);
    }

    public CloseStatementException(Throwable cause) {
        super(cause);
    }
}