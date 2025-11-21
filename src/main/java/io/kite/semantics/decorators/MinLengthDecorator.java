package io.kite.semantics.decorators;

import io.kite.semantics.TypeChecker;
import io.kite.semantics.TypeError;
import io.kite.semantics.types.DecoratorType;
import io.kite.semantics.types.SystemType;
import io.kite.semantics.types.ValueType;
import io.kite.syntax.ast.expressions.AnnotationDeclaration;
import io.kite.syntax.ast.expressions.InputDeclaration;
import io.kite.syntax.ast.expressions.OutputDeclaration;
import io.kite.syntax.literals.TypeIdentifier;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.semantics.types.DecoratorType.decorator;

public class MinLengthDecorator extends DecoratorChecker {
    public static final String NAME = "minLength";

    public MinLengthDecorator(TypeChecker checker) {
        super(checker,NAME, decorator(List.of(ValueType.Number), Set.of(
                DecoratorType.Target.INPUT,
                DecoratorType.Target.OUTPUT
        )), Set.of(SystemType.STRING, SystemType.ARRAY));
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

    private void extracted(TypeIdentifier identifier) {
        if (!isAllowedOn(identifier)) {
            String message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(getName())
                    .reset()
                    .a(" is only valid for strings and arrays. Applied to: ")
                    .fgBlue()
                    .a(identifier.getType())
                    .toString();
            throw new TypeError(message);
        }
    }
}
