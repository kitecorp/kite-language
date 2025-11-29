package cloud.kitelang.execution.decorators;

import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.ast.expressions.InputDeclaration;
import cloud.kitelang.syntax.ast.expressions.OutputDeclaration;
import cloud.kitelang.syntax.ast.statements.Statement;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import java.text.MessageFormat;
import java.util.List;

public class NonEmptyDecorator extends NumberDecorator {
    public NonEmptyDecorator(Interpreter interpreter) {
        super("nonEmpty", interpreter);
    }

    private String illegalArgumentMsg(Object value, int len, AnnotationDeclaration declaration) {
        String msg = Ansi.ansi()
                .a("Provided value ")
                .a(value)
                .a(" with length ")
                .a(len)
                .a(" is empty: \n")
                .a(interpreter.getPrinter().visit(declaration))
                .a("\n")
                .a(interpreter.getPrinter().visit((Statement) declaration.getTarget()))
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
            case String string -> {
                if (StringUtils.isBlank(string)) {
                    String msg = illegalArgumentMsg(value, StringUtils.length(string), declaration);
                    throw new IllegalArgumentException(msg);
                }
                return string;
            }
            case List<?> list -> {
                if (list.isEmpty()) {
                    String msg = illegalArgumentMsg(value, list.size(), declaration);
                    throw new IllegalArgumentException(msg);
                }
                return list;
            }
            case null, default ->
                    throw new IllegalArgumentException(MessageFormat.format("Unexpected value `{0}` in expression: {1}", value, interpreter.getPrinter().visit(declaration)));
        }

    }


}
