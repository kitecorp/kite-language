package io.kite.Frontend.Parser.Statements;

import io.kite.Frontend.Parse.Literals.NumberLiteral;
import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.Expression;
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

    public static Statement If(Expression test, Expression consequent, Expression alternate) {
        return new IfStatement(test, consequent, alternate);
    }

    public static IfStatement If(Expression test, Statement consequent, Statement alternate) {
        return new IfStatement(test,consequent, alternate);
    }
    public static Statement If(Expression test, Statement consequent) {
        return IfStatement.If(test,consequent, null);
    }

    public static Statement If(Expression test, int value) {
        return new IfStatement(test, NumberLiteral.of(value), null);
    }

    public static Statement If(Expression test, double value) {
        return new IfStatement(test, NumberLiteral.of(value), null);
    }

    public static Statement If() {
        return new IfStatement();
    }

    public static Statement If(Expression test, float value) {
        return new IfStatement(test, NumberLiteral.of(value), null);
    }

    public static Statement If(Expression test, String value) {
        return new IfStatement(test, StringLiteral.of(value), null);
    }

}
