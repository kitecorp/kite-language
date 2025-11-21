package io.kite.typechecker.types.decorators;

import io.kite.frontend.parse.literals.TypeIdentifier;
import io.kite.frontend.parser.expressions.AnnotationDeclaration;
import io.kite.frontend.parser.expressions.InputDeclaration;
import io.kite.frontend.parser.expressions.OutputDeclaration;
import io.kite.typechecker.TypeChecker;
import io.kite.typechecker.TypeError;
import io.kite.typechecker.types.DecoratorType;
import io.kite.typechecker.types.SystemType;
import io.kite.typechecker.types.ValueType;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.typechecker.types.DecoratorType.decorator;

public class MaxLengthDecorator extends DecoratorChecker {
    public static final String NAME = "maxLength";

    public MaxLengthDecorator(TypeChecker checker) {
        super(checker,NAME, decorator(List.of(ValueType.Number), Set.of(
                DecoratorType.Target.INPUT,
                DecoratorType.Target.OUTPUT
        )),Set.of(SystemType.STRING, SystemType.ARRAY));
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        validateNumber(declaration, 0, 999999);

        switch (declaration.getTarget()) {
            case InputDeclaration input -> extracted(input.getType());
            case OutputDeclaration output -> extracted(output.getType());
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        }
        return null;
    }

    private void extracted(TypeIdentifier input) {
        if (input instanceof TypeIdentifier literal) {
            if (!isAllowedOn(literal)) {
                String message = Ansi.ansi()
                        .fgYellow()
                        .a("@").a(getName())
                        .reset()
                        .a(" is only valid for strings and arrays. Applied to: ")
                        .fgBlue()
                        .a(literal.getType())
                        .toString();
                throw new TypeError(message);
            }
        }
    }
}
