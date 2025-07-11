package io.zmeu.TypeChecker.Types;

import io.zmeu.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class SchemaType extends ReferenceType {

    @ToString.Exclude
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SchemaType that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(getEnvironment(), that.getEnvironment()) &&
               Objects.equals(instances, that.instances);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), instances);
    }
}