package io.zmeu.TypeChecker.Types;

import io.zmeu.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ResourceType extends ReferenceType {
    private final SchemaType schema;
    private final TypeEnvironment environment;
    private final String name;

    public ResourceType(String typeName, SchemaType schema) {
        super(ReferenceType.Resource.getValue());
        this.name = typeName;
        this.schema = schema;
        this.environment = null;
    }

    public ResourceType(String typeName, SchemaType schema, @Nullable TypeEnvironment env) {
        super(ReferenceType.Resource.getValue());
        this.name = typeName;
        this.schema = schema;
        this.environment = env;
    }

    @Nullable
    public Type getProperty(@NotNull String fieldName) {
        return environment.get(fieldName);
    }

    @Nullable
    public Type lookup(@NotNull String fieldName) {
        return environment.get(fieldName);
    }

    public Type setProperty(@NotNull String fieldName, Type type) {
        return environment.init(fieldName, type);
    }


    public String getName() {
        return this.name;
    }
}