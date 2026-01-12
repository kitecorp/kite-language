package cloud.kitelang.execution.decorators;

import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.execution.exceptions.RuntimeError;
import cloud.kitelang.execution.values.DeferredValue;
import cloud.kitelang.syntax.annotations.CountAnnotatable;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.ast.expressions.ComponentStatement;
import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.ast.expressions.ResourceStatement;
import cloud.kitelang.syntax.ast.statements.ExpressionStatement;
import cloud.kitelang.syntax.ast.statements.ForStatement;
import cloud.kitelang.syntax.literals.SymbolIdentifier;
import org.apache.commons.lang3.Range;

import static cloud.kitelang.syntax.ast.statements.BlockExpression.block;

public class CountDecorator extends NumberDecorator {
    public CountDecorator(Interpreter interpreter) {
        super("count", interpreter);
    }

    @Override
    public Object execute(AnnotationDeclaration declaration) {
        // Evaluate the expression - it could be a literal, variable, or member expression
        var value = declaration.getValue();
        if (!(value instanceof Expression expr)) {
            throw new RuntimeError("@count requires an expression, got " + value.getClass().getSimpleName());
        }
        var evaluated = interpreter.visit(expr);

        // Check if it's a deferred cloud property
        if (evaluated instanceof DeferredValue deferred) {
            throw new RuntimeError("@count cannot use @cloud property '" + deferred.dependencyName() + "." +
                    deferred.propertyPath() + "' - this value is only available after the resource is created");
        }

        // Check if it's null (could be an unresolved cloud property)
        if (evaluated == null) {
            throw new RuntimeError("@count value cannot be null - ensure it's a number available before apply");
        }

        // Must be a number
        if (!(evaluated instanceof Number)) {
            throw new RuntimeError("@count requires a number, got " + evaluated.getClass().getSimpleName());
        }

        var count = ((Number) evaluated).intValue();
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
