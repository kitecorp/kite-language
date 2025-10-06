package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Frontend.Parser.Expressions.OutputDeclaration;
import io.kite.Frontend.Parser.Statements.Statement;
import io.kite.Runtime.Interpreter;
import org.fusesource.jansi.Ansi;

import java.text.MessageFormat;
import java.util.List;

public class UniqueDecorator extends DecoratorInterpreter {
    public UniqueDecorator() {
        super("unique");
    }

    private static String illegalArgumentMsg(Object value, Interpreter interpreter, AnnotationDeclaration declaration) {
        String msg = Ansi.ansi()
                .a("Provided list ")
                .a(value)
                .a(" has duplicate elements:\n")
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
        var value = interpreter.visit(expression);
        switch (value) {
            case List<?> list -> {
                var uniqueList = list.stream().distinct().toList();
                if (uniqueList.size() != list.size()) {
                    throw new IllegalArgumentException(illegalArgumentMsg(value, interpreter, declaration));
                }
                return list;
            }
            case null, default ->
                    throw new IllegalArgumentException(MessageFormat.format("Unexpected value `{0}` in expression: {1}", value, interpreter.getPrinter().visit(declaration)));
        }

    }


}
