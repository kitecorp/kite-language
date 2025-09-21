package io.kite.TypeChecker.Types;

import io.kite.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class SchemaType extends ReferenceType {
    public static final ReferenceType Schema = new ReferenceType(SystemType.RESOURCE);

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter
    private final TypeEnvironment instances;

    public SchemaType(String typeName, @Nullable TypeEnvironment env) {
        super(SystemType.SCHEMA, typeName, new TypeEnvironment(env));
        this.instances = new TypeEnvironment();
    }

    public Type addInstance(@NotNull String fieldName, ResourceType type) {
        return instances.init(fieldName, type);
    }

    public Type getInstance(@NotNull String fieldName) {
        return instances.lookup(fieldName);
    }


}