package io.kite.execution.decorators;

import io.kite.execution.Interpreter;
import io.kite.syntax.annotations.CountAnnotatable;
import io.kite.syntax.ast.expressions.AnnotationDeclaration;
import io.kite.syntax.ast.expressions.ComponentStatement;
import io.kite.syntax.ast.expressions.ResourceStatement;
import io.kite.syntax.ast.statements.ExpressionStatement;
import io.kite.syntax.ast.statements.ForStatement;
import io.kite.syntax.parser.literals.NumberLiteral;
import io.kite.syntax.parser.literals.SymbolIdentifier;
import org.apache.commons.lang3.Range;

import static io.kite.syntax.ast.statements.BlockExpression.block;

public class CountDecorator extends NumberDecorator {
    public CountDecorator(Interpreter interpreter) {
        super("count", interpreter);
    }

    @Override
    public Object execute(AnnotationDeclaration declaration) {
        var numberLiteral = (NumberLiteral) declaration.getValue();
        var count = (Integer) interpreter.visit(numberLiteral);
        var body = switch (declaration.getTarget()) {
            case ResourceStatement expression -> expression;
            case ComponentStatement statement -> statement;
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        };
        var forStatement = ForStatement.builder()
                .body(ExpressionStatement.expressionStatement(block(body)))
                .range(Range.of(0, count))
                .item(SymbolIdentifier.symbol("count", 0))
                .build();

        var res = interpreter.visit(forStatement);
        if (declaration.getTarget() instanceof CountAnnotatable countAnnotatable) {
            // mark the resource as counted so during the next iteration while traversing the AST
            // we know that this resource was counted and we can skip it
            countAnnotatable.setCounted(true);
        }
        return res;
    }

}
