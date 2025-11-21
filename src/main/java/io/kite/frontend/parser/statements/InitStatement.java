package io.kite.frontend.parser.statements;

import io.kite.frontend.parse.literals.Identifier;
import io.kite.frontend.parse.literals.ParameterIdentifier;
import io.kite.frontend.parser.expressions.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * <p>
 * BlockStatement
 * : '{' OptionalStatementList '}'
 * ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class InitStatement extends Statement {
    private Identifier name = Identifier.id("init");
    private List<ParameterIdentifier> params;
    private Statement body;

    private InitStatement(List<ParameterIdentifier> params, @Nullable Statement body) {
        this();
        this.params = params;
        this.body = body;
    }

    private InitStatement(List<ParameterIdentifier> params, @Nullable Expression body) {
        this(params, ExpressionStatement.expressionStatement(body));
    }

    public InitStatement() {
    }

    public static Statement of(List<ParameterIdentifier> params, Expression body) {
        return new InitStatement(params, body);
    }

    public static InitStatement of(List<ParameterIdentifier> params, Statement body) {
        return new InitStatement(params, body);
    }

    public static Statement of() {
        return new InitStatement();
    }

}
