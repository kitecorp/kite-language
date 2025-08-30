package io.kite.TypeChecker.Types;

import io.kite.TypeChecker.TypeEnvironment;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed class ReferenceType extends Type permits AnyType, ArrayType, ObjectType, ResourceType, SchemaType, UnionType {
    public static final ReferenceType Resource = new ReferenceType(SystemType.RESOURCE);

    @Getter
    protected TypeEnvironment environment;

    public ReferenceType(String typeName) {
        super(typeName);
    }

    public ReferenceType(SystemType typeName) {
        super(typeName);
    }

    public ReferenceType(String typeName, TypeEnvironment environment) {
        super(typeName);
        this.environment = environment;
    }
    public ReferenceType(SystemType typeName, String name, TypeEnvironment environment) {
        super(typeName);
        this.setValue(name);
        this.environment = environment;
    }

    public ReferenceType(SystemType typeName, TypeEnvironment environment) {
        super(typeName);
        this.environment = environment;
    }

    public static ReferenceType[] values() {
        return new ReferenceType[]{ObjectType.INSTANCE, AnyType.INSTANCE};
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
