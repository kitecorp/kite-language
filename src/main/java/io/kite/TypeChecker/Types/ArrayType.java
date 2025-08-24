package io.kite.TypeChecker.Types;

import io.kite.TypeChecker.TypeEnvironment;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ArrayType extends ReferenceType {
    public static final ArrayType ARRAY_TYPE = new ArrayType(null);
    /**
     * type of the first element in the array which defines all the other element types or type of the declared array
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
}