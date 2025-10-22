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
    private Boolean counted = null;

    public ComponentType(String name, SchemaType schema, @Nullable TypeEnvironment env) {
        super(SystemType.COMPONENT);
        this.name = name;
        this.schema = schema;
        this.environment = env;
    }

    public ComponentType() {
        this("resource", SchemaType.INSTANCE, null);
    }

    @Override
    public Boolean counted() {
        if (counted == null) {
            this.counted = Boolean.FALSE;
        }
        return counted;
    }

    @Override
    public void counted(Boolean counted) {
        this.counted = counted;
    }
}