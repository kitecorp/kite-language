package io.kite.execution.decorators;

import io.kite.analysis.visitors.SyntaxPrinter;
import io.kite.syntax.ast.expressions.AnnotationDeclaration;
import io.kite.syntax.ast.expressions.ResourceStatement;
import io.kite.syntax.literals.StringLiteral;

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
        } else {
            throw new IllegalStateException("Unexpected target for %s: ".formatted(printer.visit(declaration)));
        }
    }
}
