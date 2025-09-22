package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Frontend.Parser.Expressions.OutputDeclaration;
import io.kite.Runtime.Interpreter;

public class SensitiveDecorator extends DecoratorInterpreter {
    public SensitiveDecorator() {
        super("sensitive");
    }

    @Override
    public Object execute(Interpreter interpreter, AnnotationDeclaration declaration) {
        switch (declaration.getTarget()) {
            case InputDeclaration input -> input.setSensitive(true);
            case OutputDeclaration outputDeclaration -> outputDeclaration.setSensitive(true);
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        }
        return null;
    }
}
