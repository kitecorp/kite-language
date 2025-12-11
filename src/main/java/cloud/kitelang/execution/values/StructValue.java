package cloud.kitelang.execution.values;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.execution.environment.Environment;
import cloud.kitelang.execution.environment.IEnvironment;
import cloud.kitelang.execution.exceptions.RuntimeError;
import cloud.kitelang.syntax.literals.Identifier;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Runtime value representing a struct definition or instance.
 * Structs are nominal typed data containers.
 *
 * As a definition (registered in environment): acts as a factory for creating instances.
 * As an instance: holds the actual property values.
 */
@Data
public class StructValue implements Callable {
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Environment environment;
    private final String type;

    /**
     * Ordered list of property names for constructor argument mapping.
     */
    @EqualsAndHashCode.Exclude
    private final List<String> propertyNames = new ArrayList<>();

    /**
     * Names of properties marked with @cloud annotation.
     * These are cloud-generated and cannot be set by users.
     */
    @EqualsAndHashCode.Exclude
    private final Set<String> cloudProperties = new HashSet<>();

    /**
     * True if this is a struct definition, false if it's an instance.
     */
    @EqualsAndHashCode.Exclude
    private boolean isDefinition = true;

    public StructValue(Identifier type, Environment<Object> environment) {
        this.type = type.string();
        this.environment = environment;
    }

    private StructValue(String type, Environment<Object> environment, List<String> propertyNames, Set<String> cloudProperties) {
        this.type = type;
        this.environment = environment;
        this.propertyNames.addAll(propertyNames);
        this.cloudProperties.addAll(cloudProperties);
        this.isDefinition = false;
    }

    public static StructValue of(Identifier name, Environment environment) {
        return new StructValue(name, environment);
    }

    public static StructValue of(String name, Environment environment) {
        return new StructValue(Identifier.id(name), environment);
    }

    /**
     * Creates a new struct instance when called as a constructor.
     * Example: Point(10, 20) creates a Point instance with x=10, y=20.
     */
    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (!isDefinition) {
            throw new RuntimeError("Cannot call a struct instance as a constructor");
        }

        // Get required property count (properties without defaults that aren't @cloud)
        var requiredProps = propertyNames.stream()
                .filter(name -> !cloudProperties.contains(name) && environment.get(name) == null)
                .toList();

        if (args.size() < requiredProps.size()) {
            throw new RuntimeError("Struct '%s' requires at least %d arguments, got %d. Missing: %s"
                    .formatted(type, requiredProps.size(), args.size(), requiredProps));
        }

        if (args.size() > propertyNames.size()) {
            throw new RuntimeError("Struct '%s' has %d properties, but %d arguments were provided"
                    .formatted(type, propertyNames.size(), args.size()));
        }

        // Create a new environment for the instance, copying defaults
        var instanceEnv = new Environment<>(environment.getParent());

        // Copy default values first
        for (var propName : propertyNames) {
            var defaultValue = environment.get(propName);
            if (defaultValue != null) {
                instanceEnv.init(propName, defaultValue);
            } else {
                instanceEnv.init(propName, null);
            }
        }

        // Override with provided arguments (positional)
        for (int i = 0; i < args.size(); i++) {
            var propName = propertyNames.get(i);
            if (cloudProperties.contains(propName)) {
                throw new RuntimeError("Cannot set @cloud property '%s' during struct instantiation".formatted(propName));
            }
            instanceEnv.assign(propName, args.get(i));
        }

        return new StructValue(type, instanceEnv, propertyNames, cloudProperties);
    }

    @Override
    public int arity() {
        // Return the number of properties for arity
        return propertyNames.size();
    }

    /**
     * Adds a property name to the ordered list.
     * Must be called in declaration order during struct interpretation.
     */
    public void addPropertyName(String name) {
        propertyNames.add(name);
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

    /**
     * Marks a property as cloud-generated (@cloud annotation).
     */
    public void addCloudProperty(String propertyName) {
        cloudProperties.add(propertyName);
    }

    /**
     * Checks if a property is cloud-generated and cannot be set by users.
     */
    public boolean isCloudProperty(String propertyName) {
        return cloudProperties.contains(propertyName);
    }
}
