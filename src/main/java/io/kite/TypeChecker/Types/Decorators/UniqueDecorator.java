package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Frontend.Parser.Expressions.OutputDeclaration;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.SystemType;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class UniqueDecorator extends DecoratorChecker {

    public static final String NAME = "unique";

    public UniqueDecorator() {
        super(NAME, decorator(List.of(),
                        Set.of(DecoratorType.Target.INPUT, DecoratorType.Target.OUTPUT)
                ),
                Set.of(SystemType.ARRAY)
        );
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        switch (declaration.getTarget()) {
            case InputDeclaration input -> {
                isAllowedOnType(input.getType());
                // todo: maybe check if init == null and print an error?
            }
            case OutputDeclaration input -> {
                isAllowedOnType(input.getType());
                // todo: maybe check if init == null and print an error?
            }
            default -> {
                String message = Ansi.ansi()
                        .fgYellow()
                        .a("@").a(getName())
                        .reset()
                        .a(" can only be used on inputs and outputs")
                        .toString();
                throw new TypeError(message);
            }
        }
        return null;
    }

    private void isAllowedOnType(TypeIdentifier input) {
        if (input instanceof TypeIdentifier literal) {
            if (!isAllowedOn(literal)) {
                String message = Ansi.ansi()
                        .fgYellow()
                        .a("@").a(getName())
                        .reset()
                        .a(" is only valid for arrays. Applied to: ")
                        .fgBlue()
                        .a(literal.getType().getValue())
                        .toString();
                throw new TypeError(message);
            }
        }
    }


}
