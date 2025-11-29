package cloud.kitelang.syntax.ast;

public class SyntaxError extends RuntimeException {
    public SyntaxError() {
    }

    public SyntaxError(String message) {
        super(message);
    }

    public SyntaxError(String message, Throwable cause) {
        super(message, cause);
    }

    public SyntaxError(Throwable cause) {
        super(cause);
    }

    public SyntaxError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
