package io.kite.syntax.ast.statements;

import io.kite.syntax.ast.expressions.Expression;
import io.kite.syntax.literals.Identifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Range;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * <p>
 * BlockStatement
 * : '{' OptionalStatementList '}'
 * ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
public final class ForStatement extends Statement {
    @Nullable
    private Identifier item;
    private Identifier index; // keeps track of the index of the array: [for j, index in varName]. Index is always the second variable after first
    private Expression array; // for i in varName (typically array) or an actual array ['a', 'b', 'c']
    private Statement body;
    @Nullable
    private Range<Integer> range;

    public ForStatement() {
    }

    public boolean hasInit() {
        return item != null;
    }

    public boolean hasRange() {
        return range != null;
    }

    public List<Statement> discardBlock() {
        if (body instanceof ExpressionStatement statement) {
            if (statement.getStatement() instanceof BlockExpression expression) {
                return expression.getExpression();
            }
        }
        return null;
    }

    public boolean isBodyBlock() {
        if (body instanceof ExpressionStatement statement) {
            return statement.getStatement() instanceof BlockExpression;
        }
        return false;
    }

    public boolean hasArray() {
        return array != null;
    }

}
