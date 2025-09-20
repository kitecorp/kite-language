package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.DecoratorCallable;
import io.kite.TypeChecker.Types.ValueType;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.Target;
import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class DescriptionDecorator extends DecoratorCallable {

    public static final String DESCRIPTION = "description";

    public DescriptionDecorator() {
        super(DESCRIPTION, decorator(List.of(ValueType.String), Set.of(
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
        if (declaration.getArgs() != null && !declaration.getArgs().isEmpty()) {
            throw new TypeError("Description decorator does not accept arrays as arguments");
        } else if (declaration.getObject() != null) {
            throw new TypeError("Description decorator does not accept objects as arguments");
        } else if (declaration.getValue() != null && declaration.getValue() instanceof StringLiteral value) {
            return value.getValue();
        } else {
            throw new TypeError("Description decorator requires a string as argument");
        }
    }
}
