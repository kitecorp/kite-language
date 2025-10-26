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
    private final String name;
    @Getter
    @Setter
    private boolean counted;

    public ComponentType(String name, @Nullable TypeEnvironment parent) {
        super(SystemType.COMPONENT);
        this.name = name;
        this.environment = new TypeEnvironment(parent);
    }

    public ComponentType(String name) {
        super(SystemType.COMPONENT, null);
        this.name = name;
    }

    public ComponentType() {
        this("component", null);
    }

    public static synchronized ComponentType getInstance() {
        if (instance == null) {
            instance = new ComponentType();
        }
        return instance;
    }


}