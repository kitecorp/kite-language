package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.DecoratorCallable;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.ValueType;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class CountDecorator extends DecoratorCallable {

    public static final String COUNT = "count";

    public CountDecorator() {
        super(COUNT, decorator(List.of(ValueType.Number), Set.of(DecoratorType.Target.RESOURCE, DecoratorType.Target.COMPONENT)));
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        var number = validateNumber(declaration);
        int intValue = number.intValue();
        if (intValue < 0) {
            throw new TypeError(MessageFormat.format("Invalid count: must be greater than 0, got `{0}`", number));
        } else if (intValue >= 1000) {
            throw new TypeError(MessageFormat.format("Invalid count: must be less than 1000, got `{0}`", number));
        }
        return null;
    }

}
