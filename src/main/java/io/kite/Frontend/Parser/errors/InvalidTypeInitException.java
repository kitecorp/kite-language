package io.kite.Frontend.Parser.errors;

public class InvalidTypeInitException extends RuntimeException {
    private Object expectedValueType;
    private Object actualValueType;
    private Object actualValue;

    public InvalidTypeInitException() {
    }

    public InvalidTypeInitException(Object expectedValueType, Object actualValueType, Object actualValue) {
        super("Invalid initialization value [" + actualValue+"] of type ["+actualValueType+"] but expected ["+expectedValueType+"]");
        this.expectedValueType = expectedValueType;
        this.actualValueType = actualValueType;
        this.actualValue = actualValue;
    }

    public InvalidTypeInitException(String message, Object expectedValueType, Object actualValueType, Object actualValue) {
        super(message);
        this.expectedValueType = expectedValueType;
        this.actualValueType = actualValueType;
        this.actualValue = actualValue;
    }

    public InvalidTypeInitException(String message, Throwable cause, Object expectedValueType, Object actualValueType, Object actualValue) {
        super(message, cause);
        this.expectedValueType = expectedValueType;
        this.actualValueType = actualValueType;
        this.actualValue = actualValue;
    }

    public InvalidTypeInitException(Throwable cause, Object expectedValueType, Object actualValueType, Object actualValue) {
        super(cause);
        this.expectedValueType = expectedValueType;
        this.actualValueType = actualValueType;
        this.actualValue = actualValue;
    }

    public InvalidTypeInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, Object expectedValueType, Object actualValueType, Object actualValue) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.expectedValueType = expectedValueType;
        this.actualValueType = actualValueType;
        this.actualValue = actualValue;
    }

    public InvalidTypeInitException(String message) {
        super(message);
    }

    public InvalidTypeInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTypeInitException(Throwable cause) {
        super(cause);
    }

    public InvalidTypeInitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
