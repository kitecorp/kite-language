package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Frontend.Parser.Expressions.OutputDeclaration;
import io.kite.Frontend.Parser.Statements.Statement;
import io.kite.Runtime.Interpreter;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.text.MessageFormat;
import java.util.List;

public class NonEmptyDecorator extends NumberDecorator {
    public NonEmptyDecorator() {
        super("nonEmpty");
    }

    private static String illegalArgumentMsg(Object value, int len, Interpreter interpreter, AnnotationDeclaration declaration) {
        String msg = Ansi.ansi()
                .a("Provided value ")
                .a(value)
                .a(" with length ")
                .a(len)
                .a(" is empty: \n")
                .a(interpreter.getPrinter().visit(declaration))
                .a("\n")
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
        var value = interpreter.visit(expression);
        switch (value) {
            case String string -> {
                if (StringUtils.isBlank(string)) {
                    String msg = illegalArgumentMsg(value, StringUtils.length(string), interpreter, declaration);
                    throw new IllegalArgumentException(msg);
                }
                return string;
            }
            case List<?> list -> {
                if (list.isEmpty()) {
                    String msg = illegalArgumentMsg(value, list.size(), interpreter, declaration);
                    throw new IllegalArgumentException(msg);
                }
                return list;
            }
            case null, default ->
                    throw new IllegalArgumentException(MessageFormat.format("Unexpected value `{0}` in expression: {1}", value, interpreter.getPrinter().visit(declaration)));
        }

    }


}
