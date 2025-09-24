package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parse.Literals.NumberLiteral;
import io.kite.Frontend.Parse.Literals.SymbolIdentifier;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.ComponentStatement;
import io.kite.Frontend.Parser.Expressions.ResourceExpression;
import io.kite.Frontend.Parser.Statements.ExpressionStatement;
import io.kite.Frontend.Parser.Statements.ForStatement;
import io.kite.Frontend.Parser.Statements.Statement;
import io.kite.Frontend.annotations.CountAnnotatable;
import io.kite.Runtime.Interpreter;
import org.apache.commons.lang3.Range;

import static io.kite.Frontend.Parser.Statements.BlockExpression.block;

public class CountDecorator extends NumberDecorator {
    public CountDecorator() {
        super("count");
    }

    @Override
    public Object execute(Interpreter interpreter, AnnotationDeclaration declaration) {
        var numberLiteral = (NumberLiteral) declaration.getValue();
        var count = (Integer) interpreter.visit(numberLiteral);
        Statement body = switch (declaration.getTarget()) {
            case ResourceExpression expression -> expression;
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
            countAnnotatable.counted(true);
        }
        return res;
    }

}
