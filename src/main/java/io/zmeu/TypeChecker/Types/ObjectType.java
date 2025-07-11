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
    @Setter
    @Getter
    private boolean immutable = false;
    public ObjectType(@Nullable TypeEnvironment environment) {
        super(ReferenceType.Object.getValue(), environment);
    }

}