package io.kite.typechecker.types;

import io.kite.typechecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ArrayType extends ReferenceType {
    public static final ArrayType ARRAY_TYPE = new ArrayType(null);
    /**
     * Array's type
     */
    @Getter
    @Setter
    @NotNull
    private Type type;
    @Setter
    @Getter
    private boolean immutable = false;

    public ArrayType(@Nullable TypeEnvironment environment) {
        super(SystemType.ARRAY, environment);
    }

    public ArrayType(@Nullable TypeEnvironment environment, Type type) {
        this(environment);
        this.type = type;
    }

    public boolean isType(Type anyType) {
        return type == anyType;
    }

    public static ArrayType arrayType(Type type) {
        return new ArrayType(null, type);
    }
}