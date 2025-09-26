package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.ValueType;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class CountDecorator extends DecoratorChecker {

    public static final String COUNT = "count";

    public CountDecorator() {
        super(COUNT, decorator(List.of(ValueType.Number), Set.of(DecoratorType.Target.RESOURCE, DecoratorType.Target.COMPONENT)), Set.of());
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        var number = validateNumber(declaration, 0, 1000);

        return null;
    }

}
