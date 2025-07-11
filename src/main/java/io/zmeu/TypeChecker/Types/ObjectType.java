package io.zmeu.TypeChecker.Types;

import io.zmeu.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ObjectType extends ReferenceType {

    @Getter
    @Setter
    private boolean frozen;

    public ObjectType(@Nullable TypeEnvironment environment) {
        super(ReferenceType.Object.getValue(), environment);
    }

}