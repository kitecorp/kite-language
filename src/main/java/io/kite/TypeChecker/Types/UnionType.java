package io.kite.TypeChecker.Types;

import io.kite.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class UnionType extends ReferenceType {

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter
    private final TypeEnvironment types;

    public UnionType(String typeName, @Nullable TypeEnvironment env) {
        super(SystemType.SCHEMA, typeName, new TypeEnvironment(env));
        this.types = new TypeEnvironment();
    }

    public Type addInstance(@NotNull String fieldName, ResourceType type) {
        return types.init(fieldName, type);
    }

    public Type getInstance(@NotNull String fieldName) {
        return types.lookup(fieldName);
    }


}