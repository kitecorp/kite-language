package io.kite.frontend.parser.expressions;

import io.kite.frontend.lexer.TokenType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class LogicalExpression extends Expression {
    private Expression left;
    private Expression right;
    private TokenType operator;

    public LogicalExpression() {
    }

    public LogicalExpression(Expression left, Expression right, TokenType operator) {
        this();
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public static Expression of(Object operator, Expression left, Expression right) {
        return new LogicalExpression(left, right, TokenType.toSymbol(operator.toString()));
    }

    public static Expression or(Expression left, Expression right) {
        return new LogicalExpression(left, right, TokenType.Logical_Or);
    }

    public static Expression and(Expression left, Expression right) {
        return new LogicalExpression(left, right, TokenType.Logical_And);
    }

}
