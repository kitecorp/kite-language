package io.zmeu.TypeChecker.Types;

import io.zmeu.TypeChecker.TypeEnvironment;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed class ReferenceType extends Type permits ObjectType, ResourceType, SchemaType {
    public static final ReferenceType Resource = new ReferenceType("resource");
    public static final ReferenceType Object = new ReferenceType("object");
    public static final ReferenceType Schema = new ReferenceType("schema");

    @Getter
    protected TypeEnvironment environment;

    public ReferenceType(String typeName) {
        super(typeName);
    }

    public ReferenceType(String typeName, TypeEnvironment environment) {
        super(typeName);
        this.environment = environment;
    }

    public static ReferenceType of(String typeName) {
        return new ReferenceType(typeName);
    }

    public static ReferenceType[] values() {
        return new ReferenceType[]{Object};
    }

    @Override
    public String toString() {
        return super.getValue();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Nullable
    public Type getProperty(@NotNull String fieldName) {
        return environment.get(fieldName);
    }

    @Nullable
    public Type lookup(@NotNull String fieldName) {
        return environment.lookup(fieldName);
    }

    public Type setProperty(@NotNull String fieldName, Type type) {
        return environment.init(fieldName, type);
    }
}
