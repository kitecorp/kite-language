package io.kite.frontend.parser.expressions;

import io.kite.frontend.parse.literals.Identifier;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AssignmentExpression
 * : AdditiveExpression
 * | LeftHandSideExpression AssignmentOperator AssignmentExpression
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class ThisExpression extends Expression {
    private Identifier instance;

    private ThisExpression() {
    }

    private ThisExpression(Identifier instance) {
        this();
        this.instance = instance;
    }

    public static Expression of(Identifier operator) {
        return new ThisExpression(operator);
    }
    public static Expression of() {
        return new ThisExpression();
    }


}
