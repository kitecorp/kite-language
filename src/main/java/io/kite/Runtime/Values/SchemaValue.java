package io.kite.Runtime.Values;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.Environment.IEnvironment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Data
public class SchemaValue {
    public static final String INSTANCES = "instances";
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Environment environment;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Environment<ResourceValue> instances;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Environment<List<ResourceValue>> arrays;
    private final String type;

    public SchemaValue(Identifier type, Environment<ResourceValue> environment) {
        this.type = type.string();
        this.instances = new Environment<>(environment);
        this.environment = environment;
        this.environment.init(INSTANCES, instances);
        arrays = new Environment<>();
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

    public Object initInstance(String name, Object instance) {
        return this.instances.init(name, instance);
    }

    public ResourceValue getInstance(String name) {
        return instances.get(name);
    }

    public ResourceValue getInstanceOrElseGet(String name, Supplier<? extends ResourceValue> supplier) {
        return Optional.ofNullable(instances.get(name)).orElseGet(supplier);
    }

    public boolean addToArray(ResourceValue element) {
        var list = arrays.get(element.name());
        if (list == null) {
            list = new ArrayList<>();
            arrays.init(element.name(), list);
        }
        return list.add(element);
    }

    public boolean hasArrays() {
        return !arrays.getVariables().isEmpty();
    }

}
