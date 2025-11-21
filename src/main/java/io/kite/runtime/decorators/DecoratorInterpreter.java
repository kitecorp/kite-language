package io.kite.runtime.decorators;

import io.kite.frontend.parser.expressions.AnnotationDeclaration;
import lombok.Data;

@Data
public abstract class DecoratorInterpreter {
    private String name;

    public DecoratorInterpreter(String name) {
        this.name = name;
    }

    public abstract Object execute(AnnotationDeclaration declaration);

}
