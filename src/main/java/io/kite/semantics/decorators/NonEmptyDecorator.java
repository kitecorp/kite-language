package io.kite.semantics.decorators;

import io.kite.semantics.TypeChecker;
import io.kite.semantics.TypeError;
import io.kite.semantics.types.DecoratorType;
import io.kite.semantics.types.SystemType;
import io.kite.syntax.ast.expressions.AnnotationDeclaration;
import io.kite.syntax.ast.expressions.InputDeclaration;
import io.kite.syntax.ast.expressions.OutputDeclaration;
import io.kite.syntax.parser.literals.TypeIdentifier;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.semantics.types.DecoratorType.decorator;

public class NonEmptyDecorator extends DecoratorChecker {

    public static final String NAME = "nonEmpty";

    public NonEmptyDecorator(TypeChecker checker) {
        super(checker, NAME, decorator(List.of(),
                        Set.of(DecoratorType.Target.INPUT)
                ),
                Set.of(SystemType.STRING, SystemType.ARRAY)
        );
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        if (declaration.getValue() != null) {
            var message = Ansi.ansi()
                    .a(printer.visit(declaration))
                    .a(" can only have named arguments")
                    .toString();
            throw new TypeError(message);
        }
        switch (declaration.getTarget()) {
            case InputDeclaration input -> {
                isAllowedOnType(input.getType());
                // todo: maybe check if init == null and print an error?
            }
            case OutputDeclaration output -> {
                isAllowedOnType(output.getType());
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
                        .a(" is only valid for strings and arrays. Applied to: ")
                        .fgBlue()
                        .a(literal.getType())
                        .toString();
                throw new TypeError(message);
            }
        }
    }


}
