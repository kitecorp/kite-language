package io.zmeu.Frontend.Parser.Statements;

import io.zmeu.Frontend.Parse.Literals.*;
import io.zmeu.Frontend.Parser.Expressions.Expression;
import io.zmeu.TypeChecker.Types.ValueType;
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
public final class FunctionDeclaration extends Statement {
    private Identifier name;
    private List<ParameterIdentifier> params;
    private Statement body;
    private TypeIdentifier returnType = TypeIdentifier.type(ValueType.Void);

    private FunctionDeclaration(Identifier name,
                                List<ParameterIdentifier> params,
                                TypeIdentifier returnType,
                                @Nullable Statement body) {
        this();
        this.params = params;
        this.name = name;
        this.body = body;
        this.returnType = returnType != null ? returnType : TypeIdentifier.type(ValueType.Void);
    }

    private FunctionDeclaration(Identifier name, List<ParameterIdentifier> params, @Nullable Statement body) {
        this(name, params, null, body);
    }

    private FunctionDeclaration(Identifier name, List<ParameterIdentifier> params, @Nullable Expression body) {
        this(name, params, ExpressionStatement.expressionStatement(body));
    }

    private FunctionDeclaration() {
    }

    public static Statement fun(Identifier test, List<ParameterIdentifier> params, Expression body) {
        return new FunctionDeclaration(test, params, body);
    }

    public static Statement fun(String test, List<ParameterIdentifier> params, Expression body) {
        return FunctionDeclaration.fun(Identifier.id(test), params, body);
    }

    public static Statement fun(String test, Expression body) {
        return FunctionDeclaration.fun(Identifier.id(test), List.of(), body);
    }

    public static Statement fun(Identifier test, List<ParameterIdentifier> params, Statement body) {
        return new FunctionDeclaration(test, params, body);
    }

    public static Statement fun(Identifier test, List<ParameterIdentifier> params, TypeIdentifier returnType, Statement body) {
        return new FunctionDeclaration(test, params, returnType, body);
    }

    public static Statement fun(Identifier test, List<ParameterIdentifier> params, int value) {
        return FunctionDeclaration.fun(test, params, NumberLiteral.of(value));
    }

    public static Statement fun(Identifier test, List<ParameterIdentifier> params, double value) {
        return FunctionDeclaration.fun(test, params, NumberLiteral.of(value));
    }

    public static Statement fun() {
        return new FunctionDeclaration();
    }

    public static Statement fun(Identifier test, List<ParameterIdentifier> params, float value) {
        return FunctionDeclaration.fun(test, params, NumberLiteral.of(value));
    }

    public static Statement fun(Identifier test, List<ParameterIdentifier> params, String value) {
        return FunctionDeclaration.fun(test, params, StringLiteral.of(value));
    }

}
