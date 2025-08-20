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

    /**
     * We use set because we don't care if a value is declared multiple times in the union type
     * type x = 1 | 2 | 3 -> types = [ number ]
     */
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

    public UnionType(String typeName, @Nullable TypeEnvironment env, Expression... types) {
        super(SystemType.UNION_TYPE, typeName, env);
        this.types = Set.of(types);
    }

}