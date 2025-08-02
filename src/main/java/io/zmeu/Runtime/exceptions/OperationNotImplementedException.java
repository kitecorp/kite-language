package io.zmeu.Runtime.exceptions;

import io.zmeu.Frontend.Parser.Statements.Statement;

public class OperationNotImplementedException extends RuntimeException {
    private static final String msg = "Operation not implemented: %s";

    public OperationNotImplementedException(Statement message) {
        super(msg.formatted(message));
    }

    public OperationNotImplementedException(String message) {
        super(msg.formatted(message));
    }
}
