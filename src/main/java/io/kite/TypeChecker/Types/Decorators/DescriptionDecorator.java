package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.DecoratorCallable;
import io.kite.TypeChecker.Types.ValueType;
import org.fusesource.jansi.Ansi;

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
            String message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(getName()).a("([..])")
                    .reset()
                    .a(" does not accept arrays as arguments")
                    .toString();
            throw new TypeError(message);
        } else if (declaration.getObject() != null) {
            String message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(getName()).a("({..})")
                    .reset()
                    .a(" does not accept objects as arguments")
                    .toString();
            throw new TypeError(message);
        } else if (declaration.getValue() != null && declaration.getValue() instanceof StringLiteral value) {
            return value.getValue();
        } else {
            String message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(getName())
                    .reset()
                    .a(" requires a string as argument")
                    .toString();
            throw new TypeError(message);
        }
    }
}
