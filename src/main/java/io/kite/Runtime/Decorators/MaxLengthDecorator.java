package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Frontend.Parser.Expressions.OutputDeclaration;
import io.kite.Frontend.Parser.Statements.Statement;
import io.kite.Runtime.Interpreter;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

public class MaxLengthDecorator extends NumberDecorator {
    public MaxLengthDecorator() {
        super("maxLength");
    }

    @Override
    public Object execute(Interpreter interpreter, AnnotationDeclaration declaration) {
        return switch (declaration.getTarget()) {
            case OutputDeclaration output -> extracted(interpreter, declaration, output.getInit());
            case InputDeclaration input -> extracted(interpreter, declaration, input.getInit());
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        };
    }

    private String extracted(Interpreter interpreter, AnnotationDeclaration declaration, Expression expression) {
        var stringValue = (String) interpreter.visit(expression);
        var minArg = extractSingleNumericArg(declaration);


        ensureFinite(minArg);

        if (StringUtils.length(stringValue) > minArg.intValue()) {
            String msg = Ansi.ansi()
                    .a("Provided value ")
                    .a(stringValue)
                    .a(" with length ")
                    .a(StringUtils.length(stringValue))
                    .a(" is above the maximum length in expression: \n")
                    .a(interpreter.getPrinter().visit(declaration))
                    .a(interpreter.getPrinter().visit((Statement) declaration.getTarget()))
                    .reset()
                    .toString();
            throw new IllegalArgumentException(msg);
        }
        return stringValue;
    }


}
