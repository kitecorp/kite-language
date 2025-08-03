package io.kite.Frontend.Lexer;

public class TokenException extends RuntimeException {
    private int line;

    public TokenException() {
    }

    public TokenException(int line, String message) {
        super(String.format("line %d | Unrecognized character was found in source: %s", line, message));
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenException(Throwable cause) {
        super(cause);
    }

    public TokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
