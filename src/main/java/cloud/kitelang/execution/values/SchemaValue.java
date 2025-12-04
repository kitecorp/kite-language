package cloud.kitelang.execution.values;

import cloud.kitelang.execution.environment.Environment;
import cloud.kitelang.execution.environment.IEnvironment;
import cloud.kitelang.syntax.literals.Identifier;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class SchemaValue {
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Environment environment;
    private final String type;

    public SchemaValue(Identifier type, Environment<ResourceValue> environment) {
        this.type = type.string();
        this.environment = environment;
    }

    public static SchemaValue of(Identifier name, Environment environment) {
        return new SchemaValue(name, environment);
    }

    public static SchemaValue of(String name, Environment environment) {
        return new SchemaValue(Identifier.id(name), environment);
    }

    @NotNull
    public FunValue getMethod(String methodName) {
        return (FunValue) environment.lookup(methodName, "Method not found: " + methodName);
    }

    @Nullable
    public FunValue getMethodOrNull(String methodName) {
        return (FunValue) environment.get(methodName);
    }

    public Object assign(String varName, Object value) {
        return environment.assign(varName, value);
    }

    public Object lookup(@Nullable String varName) {
        return environment.lookup(varName);
    }

    public Object lookup(@Nullable Object varName) {
        return environment.lookup(varName);
    }

    public IEnvironment getParent() {
        return environment.getParent();
    }

    public @Nullable Object get(String key) {
        return environment.get(key);
    }

    public @Nullable boolean has(String key) {
        return environment.hasVar(key);
    }

    public Object init(String name, Object value) {
        return environment.init(name, value);
    }

}
