package io.kite.execution.values;

import io.kite.syntax.ast.expressions.Expression;
import io.kite.syntax.literals.Identifier;
import io.kite.syntax.literals.TypeIdentifier;

/**
 * Interface for var, val, outputs to implement generic dependency evaluation
 */
public interface DependencyHolder {
    boolean hasType();

    Expression getInit();

    boolean hasInit();

    Identifier getId();

    TypeIdentifier getType();
}
