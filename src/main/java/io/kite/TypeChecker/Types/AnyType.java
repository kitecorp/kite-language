package io.kite.TypeChecker.Types;

import io.kite.TypeChecker.TypeEnvironment;
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

}