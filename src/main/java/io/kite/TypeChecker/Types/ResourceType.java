package io.kite.TypeChecker.Types;

import io.kite.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ResourceType extends ReferenceType {
    @Getter
    private final SchemaType schema;
    private final String name;

    public ResourceType(String name, SchemaType schema, @Nullable TypeEnvironment env) {
        super(SystemType.RESOURCE);
        this.name = name;
        this.schema = schema;
        this.environment = env;
    }

    public String getName() {
        return this.name;
    }

}