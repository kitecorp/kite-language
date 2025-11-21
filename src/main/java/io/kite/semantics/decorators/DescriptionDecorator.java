package io.kite.semantics.decorators;

import io.kite.semantics.TypeChecker;
import io.kite.semantics.TypeError;
import io.kite.semantics.types.ValueType;
import io.kite.syntax.ast.expressions.AnnotationDeclaration;
import io.kite.syntax.literals.StringLiteral;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.semantics.types.DecoratorType.Target;
import static io.kite.semantics.types.DecoratorType.decorator;

public class DescriptionDecorator extends DecoratorChecker {

    public static final String NAME = "description";

    public DescriptionDecorator(TypeChecker checker) {
        super(checker, NAME, decorator(List.of(ValueType.String),
                        Set.of(
                                Target.RESOURCE,
                                Target.COMPONENT,
                                Target.INPUT,
                                Target.OUTPUT,
                                Target.VAR,
                                Target.SCHEMA,
                                Target.SCHEMA_PROPERTY,
                                Target.FUN
                        )),
                Set.of()
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
