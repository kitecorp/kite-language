package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.TypeChecker.Types.DecoratorType;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class SensitiveDecorator extends DecoratorChecker {

    public static final String SENSITIVE = "sensitive";

    public SensitiveDecorator() {
        super(SENSITIVE, decorator(DecoratorType.Target.INPUT, DecoratorType.Target.OUTPUT), Set.of());
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        return null;
    }
}
