package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Frontend.Parser.Expressions.OutputDeclaration;
import io.kite.Frontend.Parser.Statements.Statement;
import io.kite.Runtime.Interpreter;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.util.List;

public class MinLengthDecorator extends NumberDecorator {
    public MinLengthDecorator() {
        super("minLength");
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
    public Object execute(Interpreter interpreter, AnnotationDeclaration declaration) {
        return switch (declaration.getTarget()) {
            case OutputDeclaration output -> checkExplicitType(interpreter, declaration, output.getInit());
            case InputDeclaration input -> checkExplicitType(interpreter, declaration, input.getInit());
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        };
    }

    private Object checkExplicitType(Interpreter interpreter, AnnotationDeclaration declaration, Expression expression) {
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
