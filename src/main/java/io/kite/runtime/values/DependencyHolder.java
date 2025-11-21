package io.kite.runtime.values;

import io.kite.frontend.parse.literals.Identifier;
import io.kite.frontend.parse.literals.TypeIdentifier;
import io.kite.frontend.parser.expressions.Expression;

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
