package io.zmeu.Frontend.Parser.Expressions;

import io.zmeu.Frontend.Parser.Literals.Identifier;
import io.zmeu.Frontend.Parser.Literals.NumberLiteral;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public final class MemberExpression extends Expression {
    private boolean computed;
    private Expression object;
    private Expression property;

    public MemberExpression() {
    }

    private MemberExpression(boolean computed, Expression object, Expression property) {
        this();
        this.computed = computed;
        this.object = object;
        this.property = property;
    }

    public static Expression member(boolean computed, Expression object, Expression property) {
        return new MemberExpression(computed, object, property);
    }
    public static Expression member(boolean computed, Expression object, int property) {
        return new MemberExpression(computed, object, NumberLiteral.of(property));
    }
    public static Expression member(boolean computed, Expression object, String property) {
        return new MemberExpression(computed, object, Identifier.id(property));
    }
    public static Expression member(Expression object, String property) {
        return new MemberExpression(false, object, Identifier.id(property));
    }
    public static Expression member(boolean computed, String object, int property) {
        return new MemberExpression(computed, Identifier.id(object), NumberLiteral.of(property));
    }

    public static Expression member(boolean computed, int object, int property) {
        return new MemberExpression(computed, NumberLiteral.of(object), NumberLiteral.of(property));
    }

    public static Expression member(boolean computed, String object, String property) {
        return new MemberExpression(computed, Identifier.id(object), Identifier.id(property));
    }

    public static Expression member(String object, String property) {
        return new MemberExpression(false, Identifier.id(object), Identifier.id(property));
    }

    public static Expression member(Expression object, Expression property) {
        return new MemberExpression(false, object, property);
    }

}
