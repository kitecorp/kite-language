package cloud.kitelang.execution.values;

import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.literals.Identifier;
import cloud.kitelang.syntax.literals.TypeIdentifier;

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
