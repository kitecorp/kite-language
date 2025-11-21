package io.kite.semantics.types;

import io.kite.semantics.TypeEnvironment;
import io.kite.syntax.ast.expressions.Expression;
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

    public static UnionType unionType(String typeName, Expression... types) {
        return new UnionType(typeName, null, types);
    }

}