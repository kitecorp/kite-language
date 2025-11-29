package cloud.kitelang.syntax.ast.statements;

import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.literals.NumberLiteral;
import cloud.kitelang.syntax.literals.StringLiteral;
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
public final class IfStatement extends Statement {
    private Expression test;
    private Statement consequent;
    private Statement alternate;

    private IfStatement(Expression test, @Nullable Statement consequent, Statement alternate) {
        this.test = test;
        this.consequent = consequent;
        this.alternate = alternate;
    }
    private IfStatement(Expression test, @Nullable Expression consequent, Expression alternate) {
        this(test, ExpressionStatement.expressionStatement(consequent), ExpressionStatement.expressionStatement(alternate));
    }

    private IfStatement() {
    }

    public boolean hasElse() {
        return alternate != null;
    }

    public static Statement ifStatement(Expression test, Expression consequent, Expression alternate) {
        return new IfStatement(test, consequent, alternate);
    }

    public static IfStatement ifStatement(Expression test, Statement consequent, Statement alternate) {
        return new IfStatement(test,consequent, alternate);
    }

    public static Statement ifStatement(Expression test, Statement consequent) {
        return IfStatement.ifStatement(test, consequent, null);
    }

    public static Statement ifStatement(Expression test, int value) {
        return new IfStatement(test, NumberLiteral.of(value), null);
    }

    public static Statement ifStatement(Expression test, double value) {
        return new IfStatement(test, NumberLiteral.of(value), null);
    }

    public static Statement ifStatement() {
        return new IfStatement();
    }

    public static Statement ifStatement(Expression test, float value) {
        return new IfStatement(test, NumberLiteral.of(value), null);
    }

    public static Statement ifStatement(Expression test, String value) {
        return new IfStatement(test, StringLiteral.of(value), null);
    }

}
