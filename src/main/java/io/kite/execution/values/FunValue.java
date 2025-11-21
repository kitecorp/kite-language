package io.kite.execution.values;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;
import io.kite.execution.environment.Environment;
import io.kite.syntax.ast.expressions.Expression;
import io.kite.syntax.ast.statements.BlockExpression;
import io.kite.syntax.ast.statements.ExpressionStatement;
import io.kite.syntax.ast.statements.Statement;
import io.kite.syntax.literals.Identifier;
import io.kite.syntax.literals.ParameterIdentifier;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@Data
public class FunValue implements Callable {
    @EqualsAndHashCode.Exclude
    private Environment clojure;
    private Identifier name;
    private List<ParameterIdentifier> params;
    @EqualsAndHashCode.Exclude
    private Statement body;

    private FunValue(Identifier name, List<ParameterIdentifier> params, Statement body, Environment clojure) {
        this.name = name;
        this.params = params;
        this.body = body;
        this.clojure = clojure;
    }

    private FunValue(Identifier e) {
        this(e, Collections.emptyList(), ExpressionStatement.expressionStatement(BlockExpression.block()), new Environment());
    }

    public Identifier getRuntimeValue() {
        return name;
    }

    public static Object of(Identifier name, List<ParameterIdentifier> params, Statement body, Environment environment) {
        return new FunValue(name, params, body, environment);
    }

    public static FunValue of(String name, List<ParameterIdentifier> params, Statement body, Environment environment) {
        return (FunValue) of(Identifier.id(name), params, body, environment);
    }
    public static Object of(String name, List<ParameterIdentifier> params,  Environment environment) {
        return FunValue.of(Identifier.id(name), params, ExpressionStatement.expressionStatement(BlockExpression.block()), environment);
    }

    public static Object of(List<ParameterIdentifier> params, Statement body, Environment environment) {
        return new FunValue(null, params, body, environment);
    }

    public static Object of(Identifier string) {
        return FunValue.of(string, Collections.emptyList(), ExpressionStatement.expressionStatement(BlockExpression.block()), new Environment());
    }

    public static Object of(String string) {
        return FunValue.of(Identifier.id(string));
    }

    public static Object of(String string, Environment environment) {
        return FunValue.of(Identifier.id(string), Collections.emptyList(), ExpressionStatement.expressionStatement(BlockExpression.block()), environment);
    }

    public static Object of(Expression string) {
        if (string instanceof Identifier s)
            return new FunValue(s);
        throw new RuntimeException("Invalid variable name: " + string.toString());
    }

    @Nullable
    public String name() {
        if (name == null) return null ;
        else return name.string();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return interpreter.Call(this, args);
    }

    @Override
    public int arity() {
        return getParams().size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FunValue funValue = (FunValue) o;

        return new EqualsBuilder().append(name.string(), funValue.name.string()).append(paramsAsString(), funValue.paramsAsString()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(name.string()).append(paramsAsString()).toHashCode();
    }

    public List<String> paramsAsString() {
        return this.params.stream().map(Identifier::string).toList();
    }
}
