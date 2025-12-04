package cloud.kitelang.syntax.ast.expressions;

import cloud.kitelang.syntax.literals.Identifier;
import cloud.kitelang.syntax.literals.NumberLiteral;
import cloud.kitelang.syntax.literals.StringLiteral;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static cloud.kitelang.syntax.literals.Identifier.id;

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


    public static Expression assign(String left, String right) {
        return assign("=", id(left), StringLiteral.of(right));
    }

    public static Expression assign(String left, Expression right) {
        return assign("=", id(left), right);
    }

    public static Expression assign(String type, int right) {
        return assign("=", id(type), NumberLiteral.of(right));
    }

    public static Expression assign(Expression type, Expression name) {
        return assign("=", type, name);
    }
}
