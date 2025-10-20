package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Visitors.SyntaxPrinter;

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
