package io.kite.runtime.decorators;

import io.kite.frontend.parse.literals.StringLiteral;
import io.kite.frontend.parser.expressions.AnnotationDeclaration;
import io.kite.visitors.SyntaxPrinter;

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
