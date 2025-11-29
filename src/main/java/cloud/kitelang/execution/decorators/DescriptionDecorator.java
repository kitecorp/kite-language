package cloud.kitelang.execution.decorators;

import cloud.kitelang.analysis.visitors.SyntaxPrinter;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.literals.StringLiteral;

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
