package io.kite.typechecker.types;

import io.kite.typechecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class AnyType extends ReferenceType {
    public static final AnyType INSTANCE = new AnyType(null);

    @Getter
    @Setter
    private Object any;

    public AnyType(@Nullable TypeEnvironment env) {
        super(SystemType.ANY, env);
    }

    public AnyType(@Nullable Object object) {
        super(SystemType.ANY, null);
        this.any = object;
    }

}