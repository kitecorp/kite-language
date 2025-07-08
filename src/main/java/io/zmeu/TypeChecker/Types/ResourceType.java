package io.zmeu.TypeChecker.Types;

import io.zmeu.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ResourceType extends ReferenceType {
    private final SchemaType schema;
    private final String name;

    public ResourceType(String typeName, SchemaType schema, @Nullable TypeEnvironment env) {
        super(ReferenceType.Resource.getValue());
        this.name = typeName;
        this.schema = schema;
        this.environment = env;
    }

    public String getName() {
        return this.name;
    }

}