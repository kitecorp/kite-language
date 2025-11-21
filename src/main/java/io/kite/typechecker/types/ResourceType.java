package io.kite.typechecker.types;

import io.kite.frontend.annotations.CountAnnotatable;
import io.kite.typechecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ResourceType extends ReferenceType implements CountAnnotatable {
    public static final ResourceType INSTANCE = new ResourceType();

    @Getter
    private final SchemaType schema;
    private final String name;
    @Getter
    @Setter
    private boolean counted;

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

}