package io.kite.TypeChecker.Types;

import io.kite.Frontend.annotations.CountAnnotatable;
import io.kite.TypeChecker.TypeEnvironment;
import lombok.*;
import org.jetbrains.annotations.Nullable;



@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ComponentType extends ReferenceType implements CountAnnotatable {
    public static final ComponentType INSTANCE = new ComponentType();

    @Getter
    private final SchemaType schema;
    @Getter
    private final String name;
    @Getter
    @Setter
    private boolean counted;

    public ComponentType(String name, SchemaType schema, @Nullable TypeEnvironment env) {
        super(SystemType.COMPONENT);
        this.name = name;
        this.schema = schema;
        this.environment = env;
    }

    public ComponentType() {
        this("resource", SchemaType.INSTANCE, null);
    }

}