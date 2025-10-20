package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.ResourceStatement;
import io.kite.Visitors.SyntaxPrinter;

public class ExistingDecorator extends DecoratorInterpreter {
    private final SyntaxPrinter printer;

    public ExistingDecorator(SyntaxPrinter printer) {
        super("existing");
        this.printer = printer;
    }

    @Override
    public Object execute(AnnotationDeclaration declaration) {
        if (declaration.getTarget() instanceof ResourceStatement resource) {
            Object value = declaration.getValue();
            if (value == null) {
                throw new IllegalStateException("%s Value cannot be null".formatted(printer.visit(declaration)));
            }
            if (value instanceof StringLiteral string) {
                resource.setExisting(string.getValue());
                return resource.getExisting();
            }
            throw new IllegalStateException("%s Value must be a string literal".formatted(printer.visit(declaration)));
        }
        return null;
    }
}
