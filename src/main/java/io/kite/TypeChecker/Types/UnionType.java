package io.kite.TypeChecker.Types;

import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.TypeChecker.TypeEnvironment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;


@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class UnionType extends ReferenceType {

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Getter
    @Setter
    private Set<Expression> types;

    public UnionType(String typeName, @Nullable TypeEnvironment env) {
        this(typeName, env, new HashSet<>());
    }

    public UnionType(String typeName, @Nullable TypeEnvironment env, Set<Expression> types) {
        super(SystemType.UNION_TYPE, typeName, env);
        this.types = types;
    }

}