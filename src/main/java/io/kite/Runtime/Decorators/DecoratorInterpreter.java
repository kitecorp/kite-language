package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import lombok.Data;

@Data
public abstract class DecoratorInterpreter {
    private String name;

    public DecoratorInterpreter(String name) {
        this.name = name;
    }

    public abstract Object execute(AnnotationDeclaration declaration);

}
