package io.kite.typechecker.types;

import io.kite.typechecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class ObjectType extends ReferenceType {
    public static final ObjectType INSTANCE = new ObjectType();
    @Setter
    @Getter
    private boolean immutable = false;

    public ObjectType(@Nullable TypeEnvironment environment) {
        super(SystemType.OBJECT, environment);
    }

    public ObjectType() {
        super(SystemType.OBJECT, null);
    }

    public ObjectType(@Nullable Map<String, Type> environment) {
        super(SystemType.OBJECT, new TypeEnvironment(environment));
    }

}