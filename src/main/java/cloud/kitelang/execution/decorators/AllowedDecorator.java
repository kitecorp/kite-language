package cloud.kitelang.execution.decorators;

import cloud.kitelang.analysis.visitors.SyntaxPrinter;
import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.ast.expressions.InputDeclaration;
import cloud.kitelang.syntax.ast.statements.SchemaProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AllowedDecorator extends DecoratorInterpreter {
    private final SyntaxPrinter printer;
    private final Interpreter interpreter;

    public AllowedDecorator(Interpreter interpreter) {
        super("allowed");
        this.printer = interpreter.getPrinter();
        this.interpreter = interpreter;
    }

    private Object getTargetValue(AnnotationDeclaration declaration) {
        var target = declaration.getTarget();
        return switch (target) {
            case InputDeclaration input -> interpreter.visit(input.getInit());
            case SchemaProperty property -> property.init() != null ? interpreter.visit(property.init()) : null;
            default -> throw new IllegalStateException("Unexpected target: " + target);
        };
    }

    @Override
    public Object execute(AnnotationDeclaration declaration) {
        if (declaration.getArgs() != null) {
            var value = getTargetValue(declaration);
            // Skip validation if no default value (will be validated when instantiated)
            if (value == null) {
                return null;
            }
            var allowedValues = getAllowedValues(declaration);
            if (value instanceof List<?> list) {
                validateArrayValue(list, allowedValues);
            } else {
                validateArrayValue(List.of(value), allowedValues);
            }
        }

        return null;
    }

    private void validateArrayValue(List<?> actualValues, ArrayList<Object> allowedValues) {
        var invalid = actualValues.stream()
                .filter(value -> !allowedValues.contains(value))
                .toList();
        if (!invalid.isEmpty()) {
            throwValueNotAllowed(actualValues, allowedValues);
        }
    }

    private @NotNull ArrayList<Object> getAllowedValues(AnnotationDeclaration declaration) {
        var values = new ArrayList<>(declaration.getArgs().getItems().size());
        for (Expression item : declaration.getArgs().getItems()) {
            var visited = interpreter.visit(item);
            values.add(visited);
        }
        return values;
    }

    private void throwValueNotAllowed(Object actualValue, ArrayList<Object> allowedValues) {
        throw new IllegalArgumentException("Value `%s` is not allowed. Allowed values: %s".formatted(printer.visit(actualValue), allowedValues));
    }

}
