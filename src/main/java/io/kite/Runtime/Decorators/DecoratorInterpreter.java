package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Runtime.Interpreter;
import lombok.Data;

@Data
public abstract class DecoratorInterpreter {
    private String name;

    public abstract Object execute(Interpreter interpreter, AnnotationDeclaration declaration);

}
