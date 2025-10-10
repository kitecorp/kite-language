package io.kite.Runtime.Values;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.Environment.IEnvironment;
import io.kite.Runtime.exceptions.DeclarationExistsException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;

@Data
public class SchemaValue {
    public static final String INSTANCES = "instances";
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Environment environment;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final LinkedHashMap<String, ResourceValue> instances;
    private final String type;

    public SchemaValue(Identifier type, Environment<ResourceValue> environment) {
        this.type = type.string();
        this.instances = new LinkedHashMap<>();
        this.environment = environment;
        this.environment.init(INSTANCES, instances);
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

    public ResourceValue initInstance(ResourceValue instance) {
        var contains = this.instances.containsKey(instance.name()); // todo performance tip: replace contains with put only
        if (contains) {
            throw new DeclarationExistsException(">" + instance.name() + "< already exists in schema");
        }
        return this.instances.put(instance.name(), instance);
    }

    @Nullable
    public ResourceValue getInstance(String name) {
        return instances.get(name);
    }

    @Nullable
    public ResourceValue findInstance(String instanceName) {
        return getInstance(instanceName);
    }
}
