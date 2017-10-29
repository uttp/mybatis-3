package org.apache.ibatis.exceptions;

/**
 * Created by zhangyehui on 2017/10/28.
 */
public class ConnectionCloseException extends PersistenceException {
    private static final long serialVersionUID = 3452426936434389299L;

    public ConnectionCloseException() {
        super();
    }

    public ConnectionCloseException(String message) {
        super(message);
    }

    public ConnectionCloseException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ConnectionCloseException(Throwable throwable) {
        super(throwable);
    }
}
