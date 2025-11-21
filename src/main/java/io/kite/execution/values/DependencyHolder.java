package io.kite.execution.values;

import io.kite.syntax.ast.expressions.Expression;
import io.kite.syntax.parser.literals.Identifier;
import io.kite.syntax.parser.literals.TypeIdentifier;

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
