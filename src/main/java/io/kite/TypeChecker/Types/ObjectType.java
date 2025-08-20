package io.kite.TypeChecker.Types;

import io.kite.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ObjectType extends ReferenceType {
    public static final ObjectType INSTANCE = new ObjectType(null);
    @Setter
    @Getter
    private boolean immutable = false;

    public ObjectType(@Nullable TypeEnvironment environment) {
        super(SystemType.OBJECT, environment);
    }

}