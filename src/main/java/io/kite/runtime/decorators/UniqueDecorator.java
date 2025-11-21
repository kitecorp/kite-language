package io.kite.runtime.decorators;

import io.kite.frontend.parser.expressions.AnnotationDeclaration;
import io.kite.frontend.parser.expressions.Expression;
import io.kite.frontend.parser.expressions.InputDeclaration;
import io.kite.frontend.parser.expressions.OutputDeclaration;
import io.kite.frontend.parser.statements.Statement;
import io.kite.runtime.Interpreter;
import io.kite.visitors.SyntaxPrinter;
import org.fusesource.jansi.Ansi;

import java.text.MessageFormat;
import java.util.List;

public class UniqueDecorator extends DecoratorInterpreter {
    private final SyntaxPrinter printer;
    private final Interpreter interpreter;

    public UniqueDecorator(Interpreter interpreter) {
        super("unique");
        this.printer = interpreter.getPrinter();
        this.interpreter = interpreter;
    }

    private String illegalArgumentMsg(Object value, AnnotationDeclaration declaration) {
        String msg = Ansi.ansi()
                .a("Provided list ")
                .a(value)
                .a(" has duplicate elements:\n")
                .a(printer.visit(declaration))
                .a("\n")
                .a(printer.visit((Statement) declaration.getTarget()))
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
        var value = interpreter.visit(expression);
        switch (value) {
            case List<?> list -> {
                var uniqueList = list.stream().distinct().toList();
                if (uniqueList.size() != list.size()) {
                    throw new IllegalArgumentException(illegalArgumentMsg(value, declaration));
                }
                return list;
            }
            case null, default ->
                    throw new IllegalArgumentException(MessageFormat.format("Unexpected value `{0}` in expression: {1}", value, printer.visit(declaration)));
        }

    }


}
