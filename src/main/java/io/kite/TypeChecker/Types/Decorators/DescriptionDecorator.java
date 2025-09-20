package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.TypeChecker.Types.DecoratorCallable;
import io.kite.TypeChecker.Types.ValueType;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.Target;
import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class DescriptionDecorator extends DecoratorCallable {

    public DescriptionDecorator() {
        super("description", decorator(List.of(ValueType.String), Set.of(
                        Target.RESOURCE,
                        Target.COMPONENT,
                        Target.INPUT,
                        Target.OUTPUT,
                        Target.VAR,
                        Target.SCHEMA,
                        Target.SCHEMA_PROPERTY,
                        Target.FUN
                ))
        );
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        return null;
    }
}
