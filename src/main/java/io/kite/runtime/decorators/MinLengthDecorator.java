package io.kite.runtime.decorators;

import io.kite.frontend.parser.expressions.AnnotationDeclaration;
import io.kite.frontend.parser.expressions.Expression;
import io.kite.frontend.parser.expressions.InputDeclaration;
import io.kite.frontend.parser.expressions.OutputDeclaration;
import io.kite.frontend.parser.statements.Statement;
import io.kite.runtime.Interpreter;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.util.List;

public class MinLengthDecorator extends NumberDecorator {
    public MinLengthDecorator(Interpreter interpreter) {
        super("minLength", interpreter);
    }

    private static String illegalArgumentMsg(Object value, int len, Interpreter interpreter, AnnotationDeclaration declaration) {
        String msg = Ansi.ansi()
                .a("Provided value ")
                .a(value)
                .a(" with length ")
                .a(len)
                .a(" is below the minimum length in expression: \n")
                .a(interpreter.getPrinter().visit(declaration))
                .a(interpreter.getPrinter().visit((Statement) declaration.getTarget()))
                .reset()
                .toString();
        return msg;
    }

    @Override
    public Object execute(AnnotationDeclaration declaration) {
        return switch (declaration.getTarget()) {
            case OutputDeclaration output -> checkExplicitType(declaration, output.getInit());
            case InputDeclaration input -> checkExplicitType(declaration, input.getInit());
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        };
    }

    private Object checkExplicitType(AnnotationDeclaration declaration, Expression expression) {
        var minArg = extractSingleNumericArg(declaration);
        ensureFinite(minArg);

        var value = interpreter.visit(expression);
        switch (value) {
            case String string -> {
                if (StringUtils.length(string) < minArg.intValue()) {
                    String msg = illegalArgumentMsg(value, StringUtils.length(string), interpreter, declaration);
                    throw new IllegalArgumentException(msg);
                }
                return string;
            }
            case List<?> list -> {
                if (list.size() < minArg.intValue()) {
                    String msg = illegalArgumentMsg(value, list.size(), interpreter, declaration);
                    throw new IllegalArgumentException(msg);
                }
                return list;
            }
            case null, default ->  throw new IllegalStateException("Unexpected value: " + value);
        }

    }


}
