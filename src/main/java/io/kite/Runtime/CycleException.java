package io.kite.Runtime;

public class CycleException extends RuntimeException{
    public CycleException() {
        super();
    }

    public CycleException(String message) {
        super(message);
    }

    public CycleException(String message, Throwable cause) {
        super(message, cause);
    }

    public CycleException(Throwable cause) {
        super(cause);
    }

    protected CycleException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
