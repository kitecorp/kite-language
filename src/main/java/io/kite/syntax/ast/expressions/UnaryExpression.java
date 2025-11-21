package io.kite.syntax.ast.expressions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class UnaryExpression extends Expression {
    private Expression value;
    private String operator;

    public UnaryExpression() {
    }

    public UnaryExpression(Expression left, Object operator) {
        this();
        this.value = left;
        this.operator = operator.toString();
    }

    public static Expression of(Object operator, Expression left) {
        return new UnaryExpression(left, operator);
    }

}
