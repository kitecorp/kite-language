package io.kite.syntax.ast.statements;

import io.kite.syntax.ast.expressions.Expression;
import io.kite.syntax.literals.NumberLiteral;
import io.kite.syntax.literals.StringLiteral;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * BlockStatement
 * : { Statements? }
 * ;
 * Statements
 * : Statement* Expression
 */
@Data
@EqualsAndHashCode(callSuper = true)
public non-sealed class BlockExpression extends Expression {
    private List<Statement> expression;

    public BlockExpression(@Nullable Statement... expression) {
        this.expression = List.of(expression);
    }

    public BlockExpression(@Nullable List<Statement> expression) {
        this.expression = expression;
    }
    public BlockExpression(@Nullable Expression expression) {
        this(ExpressionStatement.expressionStatement(expression));
    }

    public BlockExpression() {
    }

    public static BlockExpression block(Expression expression) {
        return new BlockExpression(expression);
    }


    public static BlockExpression block(Statement expression) {
        return new BlockExpression(expression);
    }

    public static BlockExpression block(Statement... expression) {
        return new BlockExpression(expression);
    }

    public static BlockExpression block(List<Statement> expression) {
        return new BlockExpression(expression);
    }

    public static Expression block(int value) {
        return new BlockExpression(NumberLiteral.of(value));
    }

    public static Expression block(double value) {
        return new BlockExpression(NumberLiteral.of(value));
    }

    public static BlockExpression block() {
        return new BlockExpression(Collections.emptyList());
    }

    public static Expression block(float value) {
        return new BlockExpression(NumberLiteral.of(value));
    }

    public static Expression block(String value) {
        return new BlockExpression(StringLiteral.of(value));
    }

}
