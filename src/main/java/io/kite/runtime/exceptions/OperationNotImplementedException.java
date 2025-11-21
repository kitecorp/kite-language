package io.kite.runtime.exceptions;

import io.kite.frontend.parser.statements.Statement;

public class OperationNotImplementedException extends RuntimeException {
    private static final String msg = "Operation not implemented: %s";

    public OperationNotImplementedException(Statement message) {
        super(msg.formatted(message));
    }

    public OperationNotImplementedException(String message) {
        super(msg.formatted(message));
    }
}
