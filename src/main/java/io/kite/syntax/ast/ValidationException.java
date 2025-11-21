package io.kite.syntax.ast;

public class ValidationException extends RuntimeException {
    public ValidationException(String string) {
        super(string);
    }
}
