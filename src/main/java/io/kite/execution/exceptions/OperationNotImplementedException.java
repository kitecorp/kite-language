package io.kite.execution.exceptions;

import io.kite.syntax.ast.statements.Statement;

public class OperationNotImplementedException extends RuntimeException {
    private static final String msg = "Operation not implemented: %s";

    public OperationNotImplementedException(Statement message) {
        super(msg.formatted(message));
    }

    public OperationNotImplementedException(String message) {
        super(msg.formatted(message));
    }
}
