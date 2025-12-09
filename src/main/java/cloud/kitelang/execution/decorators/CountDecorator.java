package cloud.kitelang.execution.decorators;

import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.execution.values.ResourceValue;
import cloud.kitelang.syntax.annotations.CountAnnotatable;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.ast.expressions.ComponentStatement;
import cloud.kitelang.syntax.ast.expressions.ResourceStatement;
import cloud.kitelang.syntax.ast.statements.ExpressionStatement;
import cloud.kitelang.syntax.ast.statements.ForStatement;
import cloud.kitelang.syntax.literals.NumberLiteral;
import cloud.kitelang.syntax.literals.SymbolIdentifier;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;

import static cloud.kitelang.syntax.ast.statements.BlockExpression.block;

public class CountDecorator extends NumberDecorator {
    public CountDecorator(Interpreter interpreter) {
        super("count", interpreter);
    }

    @Override
    public Object execute(AnnotationDeclaration declaration) {
        var numberLiteral = (NumberLiteral) declaration.getValue();
        var count = (Integer) interpreter.visit(numberLiteral);
        var body = switch (declaration.getTarget()) {
            case ResourceStatement resourceStatement -> resourceStatement;
            case ComponentStatement componentStatement -> componentStatement;
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
