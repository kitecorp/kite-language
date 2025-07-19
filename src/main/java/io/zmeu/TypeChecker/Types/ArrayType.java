package io.zmeu.TypeChecker.Types;

import io.zmeu.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
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



}