package io.kite.execution.decorators;

import io.kite.analysis.visitors.SyntaxPrinter;
import io.kite.syntax.ast.expressions.AnnotationDeclaration;
import io.kite.syntax.literals.StringLiteral;

public class DescriptionDecorator extends DecoratorInterpreter {
    private final SyntaxPrinter printer;

    public DescriptionDecorator(SyntaxPrinter printer) {
        super("description");
        this.printer = printer;
    }

    @Override
    public Object execute(AnnotationDeclaration declaration) {
        if (declaration.getValue() instanceof StringLiteral literal) {
            String value = printer.visit(literal);
//            System.out.println(value);
            return value;
        }
        return null;
    }
}
