package cloud.kitelang.execution.decorators;

import cloud.kitelang.analysis.visitors.SyntaxPrinter;
import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.ast.expressions.InputDeclaration;
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

    private Object throwValueNotAllowed(AnnotationDeclaration declaration) {
        var value = declaration.getTarget();
        return switch (value) {
            case InputDeclaration input -> interpreter.visit(input.getInit());
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        };
    }

    @Override
    public Object execute(AnnotationDeclaration declaration) {
        if (declaration.getArgs() != null) {
            var value = throwValueNotAllowed(declaration);
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
