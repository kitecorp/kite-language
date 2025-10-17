package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.ComponentStatement;
import io.kite.Frontend.Parser.Expressions.ResourceStatement;
import io.kite.TypeChecker.TypeChecker;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.ValueType;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class CountDecorator extends DecoratorChecker {

    public static final String NAME = "count";
    private final TypeChecker typeChecker;

    public CountDecorator(TypeChecker typeChecker) {
        super(NAME, decorator(List.of(ValueType.Number), Set.of(DecoratorType.Target.RESOURCE, DecoratorType.Target.COMPONENT)), Set.of());
        this.typeChecker = typeChecker;
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        var count = validateNumber(declaration, 0, 1000);
        var body = switch (declaration.getTarget()) {
            case ResourceStatement expression -> expression;
            case ComponentStatement statement -> statement;
            default -> throw new TypeError("Unexpected value: " + declaration.getTarget());
        };

        return null;
    }

}
