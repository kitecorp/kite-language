package io.zmeu.TypeChecker.Types;

import io.zmeu.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ObjectType extends ReferenceType {
    @Getter
    private final TypeEnvironment environment;

    public ObjectType(@Nullable TypeEnvironment parent) {
        super(ValueType.Object.getValue());
        this.environment = new TypeEnvironment(parent);
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

}