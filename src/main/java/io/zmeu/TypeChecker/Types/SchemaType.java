package io.zmeu.TypeChecker.Types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.zmeu.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class SchemaType extends ReferenceType {

    @Getter
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final TypeEnvironment environment;
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter
    private final TypeEnvironment instances;

    public SchemaType(String typeName) {
        this(typeName, null);
    }

    public SchemaType(String typeName, @Nullable TypeEnvironment env) {
        super(typeName);
        this.environment = new TypeEnvironment(env);
        this.instances = new TypeEnvironment();
    }

    @Nullable
    public Type getProperty(@NotNull String fieldName) {
        return environment.lookup(fieldName);
    }

    public Type setProperty(@NotNull String fieldName, Type type) {
        return environment.init(fieldName, type);
    }

    public Type addInstance(@NotNull String fieldName, ResourceType type) {
        return instances.init(fieldName, type);
    }

    public Type getInstance(@NotNull String fieldName) {
        return instances.lookup(fieldName);
    }


}