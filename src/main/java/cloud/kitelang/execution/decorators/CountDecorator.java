package cloud.kitelang.execution.decorators;

import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.execution.exceptions.RuntimeError;
import cloud.kitelang.syntax.annotations.CountAnnotatable;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.ast.expressions.ComponentStatement;
import cloud.kitelang.syntax.ast.expressions.ResourceStatement;
import cloud.kitelang.syntax.ast.statements.ExpressionStatement;
import cloud.kitelang.syntax.ast.statements.ForStatement;
import cloud.kitelang.syntax.literals.SymbolIdentifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;

import static cloud.kitelang.syntax.ast.statements.BlockExpression.block;

/**
 * Decorator that creates multiple instances of a resource or component.
 *
 * <p>Usage: {@code @count(n)} where n is a number expression.
 *
 * <p>When the count expression depends on a {@code @cloud} property (e.g., {@code @count(vpc.subnetCount)}),
 * the deferred handling is done transparently by the base class {@link NumberDecorator}.
 * This decorator only needs to handle the case when the value is resolved.
 *
 * @see NumberDecorator#evaluateValueWithDeferredHandling(AnnotationDeclaration)
 */
@Slf4j
public class CountDecorator extends NumberDecorator {
    public CountDecorator(Interpreter interpreter) {
        super("count", interpreter);
    }

    /**
     * Execute the @count decorator.
     *
     * <p>Uses {@link NumberDecorator#evaluateValueWithDeferredHandling} which transparently
     * handles {@code DeferredValue} and {@code DeferredFunctionCall}. If the value is deferred,
     * a {@link cloud.kitelang.execution.values.CloudPropertyObserver} is registered and this
     * method returns null. The decorator will be re-evaluated after apply when cloud values
     * are available.
     *
     * @param declaration the annotation declaration
     * @return the result of visiting the ForStatement, or null if deferred
     */
    @Override
    public Object execute(AnnotationDeclaration declaration) {
        // Evaluate with transparent deferred handling - returns null if deferred
        var evaluated = evaluateValueWithDeferredHandling(declaration);

        // If null, the value is deferred and will be re-evaluated after apply
        if (evaluated == null) {
            log.debug("@count for '{}' is deferred - will be evaluated after apply",
                    getTargetName(declaration));
            return null;
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
            // Mark the resource as counted so during the next iteration while traversing the AST
            // we know that this resource was counted and we can skip it
            countAnnotatable.setCounted(true);
        }

        return res;
    }

    /**
     * Extract the target name for logging purposes.
     */
    private String getTargetName(AnnotationDeclaration declaration) {
        if (declaration.getTarget() instanceof ResourceStatement rs) {
            var name = rs.getName();
            return name != null ? printer.print(name) : "unknown";
        }
        if (declaration.getTarget() instanceof ComponentStatement cs) {
            var name = cs.getName();
            return name != null ? printer.print(name) : "unknown";
        }
        return "unknown";
    }
}
