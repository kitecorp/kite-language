package io.kite.TypeChecker.Types;

import io.kite.Frontend.Parse.Literals.NumberLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.TypeChecker.TypeError;
import lombok.Data;

import java.text.MessageFormat;
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

    protected Number validateNumber(AnnotationDeclaration declaration) {
        if (declaration.getArgs() != null && !declaration.getArgs().isEmpty()) {
            throw new TypeError(MessageFormat.format("@{0} does not accept arrays as arguments", name));
        } else if (declaration.getObject() != null) {
            throw new TypeError(MessageFormat.format("@{0} does not accept objects as arguments", name));
        } else if (declaration.getValue() == null) {
            throw new TypeError(MessageFormat.format("@{0} requires a number as argument", name));
        }

        var value = declaration.getValue();
        var number = switch (value) {
            case NumberLiteral literal -> literal.getValue();
            default -> throw new TypeError("Invalid count value: " + value);
        };
        return number;
    }

}
