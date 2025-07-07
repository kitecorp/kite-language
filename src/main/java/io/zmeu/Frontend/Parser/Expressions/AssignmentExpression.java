package io.zmeu.Frontend.Parser.Expressions;

import io.zmeu.Frontend.Parse.Literals.Identifier;
import io.zmeu.Frontend.Parse.Literals.NumberLiteral;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static io.zmeu.Frontend.Parse.Literals.Identifier.id;

@Data
@EqualsAndHashCode(callSuper = true)
public final class AssignmentExpression extends Expression {
    /**
     * Must either be Identifier or MemberExpression
     */
    private Expression left;
    private Expression right;
    private Object operator;

    private AssignmentExpression() {
    }

    private AssignmentExpression(Expression left, Expression right, Object operator) {
        this();
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public static Expression assign(Expression left, Expression right, Object operator) {
        return new AssignmentExpression(left, right, operator);
    }

    public static Expression assign(Expression left, int right, Object operator) {
        return new AssignmentExpression(left, NumberLiteral.of(right), operator);
    }

    public static Expression assign(Object operator, Expression left, Expression right) {
        return new AssignmentExpression(left, right, operator);
    }
    public static Expression assign(Object operator, String left, Expression right) {
        return new AssignmentExpression(id(left), right, operator);
    }

    public static Expression assignment(Object operator, Expression left, Expression right) {
        return new AssignmentExpression(left, right, operator);
    }

    public static Expression assign(Object operator, Expression left, String right) {
        return new AssignmentExpression(left, Identifier.id(right), operator);
    }

    public static Expression assign(Object operator, Expression left, int right) {
        return new AssignmentExpression(left, NumberLiteral.of(right), operator);
    }


}
