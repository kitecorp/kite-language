package io.zmeu.Frontend.Parser.Expressions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class GroupExpression extends Expression {
    private Expression expression;

    public GroupExpression(Expression expression) {
        this.expression = expression;
    }


}
