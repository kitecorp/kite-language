package io.kite.TypeChecker.Types;

import io.kite.Frontend.annotations.CountAnnotatable;
import io.kite.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;


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

    public ComponentType(String type, String name, @Nullable TypeEnvironment parent) {
        super(SystemType.COMPONENT);
        this.type = type;
        this.name = name;
        this.environment = new TypeEnvironment(name, parent);
    }

    public ComponentType(String type, String name) {
        this(type, name, new TypeEnvironment(name));
    }

    public ComponentType(String type, @Nullable TypeEnvironment parent) {
        this(type, null, new TypeEnvironment(parent));
    }

    public ComponentType(String type) {
        this(type, null, new TypeEnvironment());
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


}