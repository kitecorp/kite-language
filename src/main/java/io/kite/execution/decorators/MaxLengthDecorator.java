package io.kite.execution.decorators;

import io.kite.execution.Interpreter;
import io.kite.syntax.ast.expressions.AnnotationDeclaration;
import io.kite.syntax.ast.expressions.Expression;
import io.kite.syntax.ast.expressions.InputDeclaration;
import io.kite.syntax.ast.expressions.OutputDeclaration;
import io.kite.syntax.ast.statements.Statement;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.util.List;

public class MaxLengthDecorator extends NumberDecorator {

    public MaxLengthDecorator(Interpreter interpreter) {
        super("maxLength", interpreter);
    }

    private  String illegalArgumentMsg(Object value, int len, AnnotationDeclaration declaration) {
        String msg = Ansi.ansi()
                .a("Provided value ")
                .a(value)
                .a(" with length ")
                .a(len)
                .a(" is above the maximum length in expression: \n")
                .a(printer.visit(declaration))
                .a(printer.visit((Statement) declaration.getTarget()))
                .reset()
                .toString();
        throw new IllegalArgumentException(msg);
    }

    @Override
    public Object execute(AnnotationDeclaration declaration) {
        return switch (declaration.getTarget()) {
            case OutputDeclaration output -> extracted( declaration, output.getInit());
            case InputDeclaration input -> extracted( declaration, input.getInit());
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        };
    }

    private Object extracted(AnnotationDeclaration declaration, Expression expression) {
        var minArg = extractSingleNumericArg(declaration);
        ensureFinite(minArg);

        var value = interpreter.visit(expression);
        switch (value) {
            case String string -> {
                if (StringUtils.length(string) > minArg.intValue()) {
                    String msg = illegalArgumentMsg(value, StringUtils.length(string),  declaration);
                    throw new IllegalArgumentException(msg);
                }
                return string;
            }
            case List<?> list -> {
                if (list.size() > minArg.intValue()) {
                    String msg = illegalArgumentMsg(value, list.size(),  declaration);
                    throw new IllegalArgumentException(msg);
                }
                return list;
            }
            case null, default -> throw new IllegalStateException("Unexpected value: " + value);
        }
    }

}
