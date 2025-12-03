package cloud.kitelang.syntax.ast.expressions;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class LogicalExpression extends Expression {
    private Expression left;
    private Expression right;
    private String operator;

    public LogicalExpression() {
    }

    public LogicalExpression(Expression left, Expression right, String operator) {
        this();
        this.left = left;
        this.right = right;
        this.operator = operator;
    }

    public static Expression of(Object operator, Expression left, Expression right) {
        return new LogicalExpression(left, right, operator.toString());
    }

    public static Expression logical(Object operator, Expression left, Expression right) {
        return LogicalExpression.of(operator, left, right);
    }

    public static Expression or(Expression left, Expression right) {
        return new LogicalExpression(left, right, "||");
    }

    public static Expression and(Expression left, Expression right) {
        return new LogicalExpression(left, right, "&&");
    }

}
