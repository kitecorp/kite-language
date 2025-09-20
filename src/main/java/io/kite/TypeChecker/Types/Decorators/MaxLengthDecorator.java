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

public class MaxLengthDecorator extends DecoratorCallable {
    public static final String MAX_LENGTH = "maxLength";

    public MaxLengthDecorator() {
        super(MAX_LENGTH, decorator(List.of(ValueType.Number), Set.of(
                DecoratorType.Target.INPUT,
                DecoratorType.Target.OUTPUT
        )));
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        var number = validateNumber(declaration);
        var value = number.longValue();
        if (value < 0) {
            throw new TypeError(MessageFormat.format("Invalid count: must be greater than 0, got `{0}`", number));
        } else if (value >= 9999999) {
            throw new TypeError(MessageFormat.format("Invalid count: must be less than 9999999, got `{0}`", number));
        }
        return null;
    }
}
