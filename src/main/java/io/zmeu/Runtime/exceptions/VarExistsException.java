package io.zmeu.Runtime.exceptions;

public class VarExistsException extends RuntimeException {
    private static final String msg = "Variable is already declared: %s";

    public VarExistsException(String message) {
        super(msg.formatted(message));
    }
}
