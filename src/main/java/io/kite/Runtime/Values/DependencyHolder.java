package io.kite.Runtime.Values;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Expressions.Expression;

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
