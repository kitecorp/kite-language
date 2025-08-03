package io.kite.Frontend.Parser.Expressions;

import io.kite.Frontend.Parse.Literals.Identifier;
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
