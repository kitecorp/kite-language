package io.kite.syntax.ast.expressions;

public class Expressions {
    public static Expression logical(Object operator, Expression left, Expression right) {
        return LogicalExpression.of(operator, left, right);
    }

    public static Expression binary(String identifier, int left, String operator) {
        return BinaryExpression.binary(identifier, left, operator);
    }
}
