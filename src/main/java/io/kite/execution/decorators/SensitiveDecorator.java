package io.kite.execution.decorators;

import io.kite.syntax.ast.expressions.AnnotationDeclaration;
import io.kite.syntax.ast.expressions.InputDeclaration;
import io.kite.syntax.ast.expressions.OutputDeclaration;

public class SensitiveDecorator extends DecoratorInterpreter {
    public SensitiveDecorator() {
        super("sensitive");
    }

    @Override
    public Object execute(AnnotationDeclaration declaration) {
        switch (declaration.getTarget()) {
            case InputDeclaration input -> input.setSensitive(true);
            case OutputDeclaration outputDeclaration -> outputDeclaration.setSensitive(true);
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        }
        return null;
    }
}
