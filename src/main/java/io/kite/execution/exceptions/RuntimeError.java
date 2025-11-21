package io.kite.execution.exceptions;

public class RuntimeError extends RuntimeException {
    public RuntimeError(String message) {
        super(message);
    }
}