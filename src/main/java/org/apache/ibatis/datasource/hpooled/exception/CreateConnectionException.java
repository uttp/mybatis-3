package org.apache.ibatis.datasource.hpooled.exception;

import org.apache.ibatis.exceptions.PersistenceException;

/**
 * Created by zhangyehui on 2017/11/3.
 */
public class CreateConnectionException extends PersistenceException {
    private static final long serialVersionUID = -6128706529225136743L;

    public CreateConnectionException() {
        super();
    }

    public CreateConnectionException(String message) {
        super(message);
    }

    public CreateConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreateConnectionException(Throwable cause) {
        super(cause);
    }
}
