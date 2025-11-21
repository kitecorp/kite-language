package io.kite.runtime.decorators;

import io.kite.frontend.annotations.CountAnnotatable;
import io.kite.frontend.parse.literals.NumberLiteral;
import io.kite.frontend.parse.literals.SymbolIdentifier;
import io.kite.frontend.parser.expressions.AnnotationDeclaration;
import io.kite.frontend.parser.expressions.ComponentStatement;
import io.kite.frontend.parser.expressions.ResourceStatement;
import io.kite.frontend.parser.statements.ExpressionStatement;
import io.kite.frontend.parser.statements.ForStatement;
import io.kite.runtime.Interpreter;
import org.apache.commons.lang3.Range;

import static io.kite.frontend.parser.statements.BlockExpression.block;

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
