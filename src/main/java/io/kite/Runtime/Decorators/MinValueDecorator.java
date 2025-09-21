package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Frontend.Parser.Expressions.OutputDeclaration;
import io.kite.Runtime.Interpreter;

public class MinValueDecorator extends NumberDecorator {
    @Override
    public Object execute(Interpreter interpreter, AnnotationDeclaration declaration) {
        return switch (declaration.getTarget()) {
            case OutputDeclaration output -> extracted(interpreter, declaration, output.getInit());
            case InputDeclaration input -> extracted(interpreter, declaration, input.getInit());
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        };
    }

    private Number extracted(Interpreter interpreter, AnnotationDeclaration declaration, Expression expression) {
        var numberValue = (Number) interpreter.visit(expression);
        var minArg = extractSingleNumericArg(declaration);


        ensureFinite(numberValue);
        ensureFinite(minArg);

        if (compareNumbers(numberValue, minArg) < 0) {
            String msg = "Value " + numberValue + " is below the minimum (" + minArg + ").";
            throw new IllegalArgumentException(msg);
        }
        return numberValue;
    }


}
