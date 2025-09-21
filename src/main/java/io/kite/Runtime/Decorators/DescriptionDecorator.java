package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Runtime.Interpreter;

public class DescriptionDecorator extends NumberDecorator {
    public DescriptionDecorator() {
        super("description");
    }

    @Override
    public Object execute(Interpreter interpreter, AnnotationDeclaration declaration) {
        if (declaration.getValue() instanceof StringLiteral literal) {
            String value = literal.getValue();
            System.out.println(value);
            return value;
        }
        return null;
    }
}
