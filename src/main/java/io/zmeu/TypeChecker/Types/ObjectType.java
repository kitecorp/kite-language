package io.zmeu.TypeChecker.Types;

import io.zmeu.Frontend.Parse.Literals.Identifier;
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
    private Identifier name;

    public ObjectType(@Nullable TypeEnvironment parent) {
        super(ReferenceType.Object.getValue(), parent);
    }

    public ObjectType(Identifier identifier, @Nullable TypeEnvironment parent) {
        this(parent);
        this.name = identifier;
    }


}