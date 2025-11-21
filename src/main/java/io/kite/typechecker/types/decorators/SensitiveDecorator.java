package io.kite.typechecker.types.decorators;

import io.kite.frontend.parser.expressions.AnnotationDeclaration;
import io.kite.typechecker.TypeChecker;
import io.kite.typechecker.TypeError;
import io.kite.typechecker.types.DecoratorType;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.typechecker.types.DecoratorType.decorator;

public class SensitiveDecorator extends DecoratorChecker {

    public static final String NAME = "sensitive";

    public SensitiveDecorator(TypeChecker checker) {
        super(checker, NAME, decorator(DecoratorType.Target.INPUT, DecoratorType.Target.OUTPUT), Set.of());
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        if (declaration.getValue() != null) {
            var message = Ansi.ansi()
                    .a(printer.visit(declaration))
                    .a(" can only have named arguments")
                    .toString();
            throw new TypeError(message);
        } else if (doesNotHaveArguments(declaration)) {
            var message = Ansi.ansi()
                    .a(printer.visit(declaration))
                    .a(" is missing arguments")
                    .toString();
            throw new TypeError(message);
        }
        return null;
    }
}
