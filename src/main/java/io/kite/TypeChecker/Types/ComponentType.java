package io.kite.TypeChecker.Types;

import io.kite.Frontend.annotations.CountAnnotatable;
import io.kite.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ComponentType extends ReferenceType implements CountAnnotatable {
    private static ComponentType instance;

    @Getter
    private String type;
    @Getter
    private String name;

    @Getter
    @Setter
    private boolean counted;

    public ComponentType(String type, String name, @Nullable TypeEnvironment environment) {
        super(SystemType.COMPONENT);
        this.type = type;
        this.name = name;
        this.environment = environment;
        if (environment != null && !environment.hasName()) {
            environment.setName(name);
        }
    }

    public ComponentType(String type, String name) {
        this(type, name, null);
    }

    public ComponentType(String type, @Nullable TypeEnvironment parent) {
        this(type, null, parent);
    }

    public ComponentType(String type) {
        this(type, null, null);
    }

    public ComponentType() {
        this(null);
    }

    public static synchronized ComponentType getInstance() {
        if (instance == null) {
            instance = new ComponentType("component");
        }
        return instance;
    }

    public Type lookup(String key) {
        return environment.lookup(key);
    }

    public boolean has(String key) {
        return environment.lookupKey(key);
    }

    public Optional<ComponentType> getNestedComponent(String name) {
        Type type = lookup(name);
        return type instanceof ComponentType ? Optional.of((ComponentType) type) : Optional.empty();
    }

    public Optional<ResourceType> getResource(String name) {
        Type type = lookup(name);
        return type instanceof ResourceType ? Optional.of((ResourceType) type) : Optional.empty();
    }

    // Or more general
    public <T extends Type> Optional<T> get(String name, Class<T> expectedType) {
        Type type = lookup(name);
        return expectedType.isInstance(type) ? Optional.of(expectedType.cast(type)) : Optional.empty();
    }

}