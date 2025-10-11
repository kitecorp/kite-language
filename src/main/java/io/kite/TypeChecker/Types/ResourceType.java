package io.kite.TypeChecker.Types;

import io.kite.Frontend.annotations.CountAnnotatable;
import io.kite.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ResourceType extends ReferenceType implements CountAnnotatable {
    public static final ResourceType INSTANCE = new ResourceType();

    @Getter
    private final SchemaType schema;
    private final String name;
    private Boolean counted = null;

    public ResourceType(String name, SchemaType schema, @Nullable TypeEnvironment env) {
        super(SystemType.RESOURCE);
        this.name = name;
        this.schema = schema;
        this.environment = env;
    }

    public ResourceType() {
        this("resource", SchemaType.INSTANCE, null);
    }

    public String getName() {
        return this.name;
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