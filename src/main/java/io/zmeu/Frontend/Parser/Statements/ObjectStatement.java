package io.zmeu.Frontend.Parser.Statements;

import io.zmeu.Frontend.Parser.Expressions.ObjectExpression;
import io.zmeu.Frontend.Parser.Literals.Identifier;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

import static io.zmeu.Frontend.Parser.Expressions.ObjectExpression.object;

/**
 * <p>
 * VariableStatement
 * : var Identity (, Identity)* Assignment Expression
 * ;
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public final class ObjectStatement extends Statement {
    private List<ObjectExpression> declarations;

    private ObjectStatement(@Nullable List<ObjectExpression> declarations) {
        this.declarations = declarations;
    }

    private ObjectStatement() {
        this(Collections.emptyList());
    }

    public static Statement objectStatement(ObjectExpression... expression) {
        return new ObjectStatement(List.of(expression));
    }

    public static Statement objectStatement(Identifier expression) {
        return new ObjectStatement(List.of(object(expression)));
    }

    public static Statement objectStatement(List<ObjectExpression> expression) {
        return new ObjectStatement(expression);
    }

    public static Statement objectStatement() {
        return new ObjectStatement();
    }

}
