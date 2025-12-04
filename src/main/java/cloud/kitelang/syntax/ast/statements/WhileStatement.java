package cloud.kitelang.syntax.ast.statements;

import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.literals.NumberLiteral;
import cloud.kitelang.syntax.literals.StringLiteral;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * BlockStatement
 * : '{' OptionalStatementList '}'
 * ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public final class WhileStatement extends Statement {
    private Expression test;
    private Statement body;

    public WhileStatement(Expression test, @Nullable Statement body) {
        this();
        this.test = test;
        this.body = body;
    }
    public WhileStatement(Expression test, @Nullable Expression body) {
        this(test, ExpressionStatement.expressionStatement(body));
    }

    public WhileStatement() {
    }

    public static Statement of(Expression test, Expression consequent) {
        return new WhileStatement(test, consequent);
    }

    public static WhileStatement of(Expression test, Statement consequent) {
        return new WhileStatement(test,consequent);
    }

    public static Statement of(Expression test, int value) {
        return new WhileStatement(test, NumberLiteral.of(value));
    }

    public static Statement of(Expression test, double value) {
        return new WhileStatement(test, NumberLiteral.of(value));
    }

    public static Statement of() {
        return new WhileStatement();
    }

    public static Statement of(Expression test, float value) {
        return new WhileStatement(test, NumberLiteral.of(value));
    }

    public static Statement of(Expression test, String value) {
        return new WhileStatement(test, StringLiteral.of(value));
    }

}
