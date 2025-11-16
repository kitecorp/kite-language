package io.kite.Frontend.Parser.Statements;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.NumberLiteral;
import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ReturnStatement extends Statement {
    private Identifier name;
    private Statement argument;

    public ReturnStatement(@Nullable Statement argument) {
        this();
        this.argument = argument;
    }
    public ReturnStatement(@Nullable Expression argument) {
        this(ExpressionStatement.expressionStatement(argument));
    }

    public ReturnStatement() {
    }


    public static ReturnStatement funReturn(Expression body) {
        return new ReturnStatement(body);
    }

    public static Statement funReturn(Statement body) {
        return new ReturnStatement(body);
    }

    public static Statement funReturn(int value) {
        return new ReturnStatement(NumberLiteral.of(value));
    }

    public static Statement funReturn(double value) {
        return new ReturnStatement(NumberLiteral.of(value));
    }

    public static Statement funReturn() {
        return new ReturnStatement();
    }

    public static Statement funReturn(float value) {
        return new ReturnStatement(NumberLiteral.of(value));
    }

    public static Statement funReturn(String value) {
        return new ReturnStatement(StringLiteral.of(value));
    }


    public boolean hasArgument() {
        return argument != null;
    }
}
