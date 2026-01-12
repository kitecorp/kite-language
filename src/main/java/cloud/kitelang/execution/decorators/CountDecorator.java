package cloud.kitelang.execution.decorators;

import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.execution.exceptions.RuntimeError;
import cloud.kitelang.execution.values.DeferredResourceTemplate;
import cloud.kitelang.execution.values.DeferredValue;
import cloud.kitelang.syntax.annotations.CountAnnotatable;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.ast.expressions.ComponentStatement;
import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.ast.expressions.ResourceStatement;
import cloud.kitelang.syntax.ast.statements.ExpressionStatement;
import cloud.kitelang.syntax.ast.statements.ForStatement;
import cloud.kitelang.syntax.literals.Identifier;
import cloud.kitelang.syntax.literals.SymbolIdentifier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;

import java.util.Set;

import static cloud.kitelang.syntax.ast.statements.BlockExpression.block;

@Slf4j
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

        // Check if it's a deferred cloud property - create template for apply-time resolution
        if (evaluated instanceof DeferredValue deferred) {
            return createDeferredTemplate(declaration, expr, deferred);
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

    /**
     * Creates a deferred resource template when @count depends on a @cloud property.
     * The template will be instantiated during apply after the dependency is created.
     *
     * @param declaration the annotation declaration
     * @param countExpr   the count expression to re-evaluate during apply
     * @param deferred    the deferred value that caused this
     * @return the deferred template (also registered with interpreter)
     */
    private DeferredResourceTemplate createDeferredTemplate(
            AnnotationDeclaration declaration,
            Expression countExpr,
            DeferredValue deferred) {

        if (!(declaration.getTarget() instanceof ResourceStatement resourceStatement)) {
            throw new RuntimeError("@count with deferred value is only supported on resources, not " +
                    declaration.getTarget().getClass().getSimpleName());
        }

        var resourceName = resourceStatement.getName();
        var templateName = getTemplateName(resourceName);
        var resourceType = resourceStatement.getType();

        var template = new DeferredResourceTemplate(
                templateName,
                countExpr,
                resourceStatement,
                Set.of(deferred.dependencyName()),
                resourceType,
                deferred
        );

        // Register with interpreter for tracking
        interpreter.addDeferredTemplate(template);

        // Mark as counted so the resource isn't evaluated again
        resourceStatement.setCounted(true);

        log.info("Created deferred resource template: {} ({})", template.templateName(), template.getDeferredReason());

        return template;
    }

    /**
     * Extract the template name string from the resource name expression.
     *
     * @param nameExpression the name expression (could be Identifier, StringLiteral, etc.)
     * @return the string representation of the name
     */
    private String getTemplateName(Expression nameExpression) {
        if (nameExpression == null) {
            return "unknown";
        }
        if (nameExpression instanceof Identifier id) {
            return id.string();
        }
        // For other expression types, evaluate and convert to string
        var evaluated = interpreter.visit(nameExpression);
        return evaluated != null ? evaluated.toString() : "unknown";
    }
}
