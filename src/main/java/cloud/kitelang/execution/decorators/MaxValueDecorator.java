package cloud.kitelang.execution.decorators;

import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.ast.expressions.InputDeclaration;
import cloud.kitelang.syntax.ast.expressions.OutputDeclaration;
import cloud.kitelang.syntax.ast.statements.Statement;
import org.fusesource.jansi.Ansi;

public class MaxValueDecorator extends NumberDecorator {
    public MaxValueDecorator(Interpreter interpreter) {
        super("maxValue", interpreter);
    }

    @Override
    public Object execute(AnnotationDeclaration declaration) {
        return switch (declaration.getTarget()) {
            case OutputDeclaration output -> extracted(declaration, output.getInit());
            case InputDeclaration input -> extracted(declaration, input.getInit());
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        };
    }

    private Number extracted(AnnotationDeclaration declaration, Expression expression) {
        var numberValue = (Number) interpreter.visit(expression);
        var minArg = extractSingleNumericArg(declaration);


        ensureFinite(numberValue);
        ensureFinite(minArg);

        if (compareNumbers(numberValue, minArg) > 0) {
            String msg = Ansi.ansi()
                    .a("Provided value ")
                    .a(numberValue)
                    .a(" is above the maximum in expression: \n")
                    .a(interpreter.getPrinter().visit(declaration))
                    .a(interpreter.getPrinter().visit((Statement) declaration.getTarget()))
                    .reset()
                    .toString();
            throw new IllegalArgumentException(msg);
        }
        return numberValue;
    }


}
