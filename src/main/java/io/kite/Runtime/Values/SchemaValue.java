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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

@Data
public class SchemaValue {
    public static final String INSTANCES = "instances";
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Environment environment;
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final LinkedList<ResourceValue> instances;
    /**
     * used to do quick(constant time) checks if a resource is already created in.
     * Useful in for loops ex: for i in ['item', 'item'] should throw duplicate error
     * Alternative is to iterate the list of instances in linear time
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private final Set<String> instanceNames;
    private final String type;

    public SchemaValue(Identifier type, Environment<ResourceValue> environment) {
        this.type = type.string();
        this.instances = new LinkedList<>();
        this.instanceNames = new HashSet<>();
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

    public boolean initInstance(ResourceValue instance) {
        var contains = this.instanceNames.add(instance.name());
        if (!contains){
            throw new DeclarationExistsException(">" + instance.name() + "< already exists in schema");
        }
        return this.instances.add(instance);
    }

    @Nullable
    public ResourceValue getInstance(String name) {
        if (!instanceNames.contains(name)) return null;

        for (ResourceValue instance : instances) {
            if (instance.name().equals(name)) {
                return instance;
            }
        }
        return null;
    }

    @Nullable
    public ResourceValue findInstance(String instanceName) {
        return getInstance(instanceName);
    }
}
