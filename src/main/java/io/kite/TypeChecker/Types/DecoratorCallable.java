package io.kite.TypeChecker.Types;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public abstract class DecoratorCallable {
    private final String name;
    private final DecoratorType type;

    public DecoratorCallable(String name, DecoratorType type) {
        this.name = name;
        this.type = type;
    }

    public abstract Object validate(AnnotationDeclaration declaration, List<Object> args);


    public Object validate(AnnotationDeclaration declaration, Object... args) {
        return validate(declaration, List.of(args));
    }

    public int arity() {
        return 0;
    }

    public Set<DecoratorType.Target> targets() {
        return type.getTargets();
    }

    public String targetString() {
        return type.targetString();
    }
}
