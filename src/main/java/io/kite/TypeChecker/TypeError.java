package io.kite.TypeChecker;

import io.kite.Frontend.Parser.Expressions.Expression;

public class TypeError extends RuntimeException {
    public TypeError(Expression t1, Expression t2) {
        super("Expression " + t1.getClass().getSimpleName() + " but expected " + t2.getClass().getSimpleName());
    }

    public TypeError(Expression t1) {
        super("Expression is of type: " + t1.getClass().getSimpleName());
    }
    public TypeError(String string) {
        super(string);
    }
}
