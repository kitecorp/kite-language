package cloud.kitelang.syntax.ast.statements;

import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.literals.NumberLiteral;
import cloud.kitelang.syntax.literals.StringLiteral;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * An expression statement is one that evaluates an expression and ignores its result.
 * As a rule, an expression statement's purpose is to trigger the effects of evaluating its expression.
 * ExpressionStatement
 * : Expression '\n'
 * | Statement
 * ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class ExpressionStatement extends Statement {
    private Expression statement;

    private ExpressionStatement(Expression statement) {
        this.statement = statement;
    }

    private ExpressionStatement() {
        this(null);
    }

    public static ExpressionStatement expressionStatement(Expression expression) {
        return new ExpressionStatement(expression);
    }

    public static Statement expressionStatement() {
        return new ExpressionStatement();
    }

    public static Statement expressionStatement(int value) {
        return new ExpressionStatement(NumberLiteral.of(value));
    }

    public static Statement expressionStatement(double value) {
        return new ExpressionStatement(NumberLiteral.of(value));
    }

    public static Statement expressionStatement(float value) {
        return new ExpressionStatement(NumberLiteral.of(value));
    }

    public static Statement expressionStatement(String value) {
        return new ExpressionStatement(StringLiteral.of(value));
    }

}
