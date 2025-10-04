package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Runtime.Interpreter;
import io.kite.Visitors.SyntaxPrinter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AllowedDecorator extends DecoratorInterpreter {
    private SyntaxPrinter printer = new SyntaxPrinter();

    public AllowedDecorator() {
        super("allowed");
    }

    private static Object throwValueNotAllowed(Interpreter interpreter, AnnotationDeclaration declaration) {
        var value = declaration.getTarget();
        return switch (value) {
            case InputDeclaration input -> interpreter.visit(input);
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        };
    }

    @Override
    public Object execute(Interpreter interpreter, AnnotationDeclaration declaration) {
        if (declaration.getArgs() != null) {
            var value = throwValueNotAllowed(interpreter, declaration);
            var allowedValues = getAllowedValues(interpreter, declaration);
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

    private static @NotNull ArrayList<Object> getAllowedValues(Interpreter interpreter, AnnotationDeclaration declaration) {
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
