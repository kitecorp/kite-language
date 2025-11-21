package io.kite.typechecker.types.decorators;

import io.kite.frontend.parser.expressions.AnnotationDeclaration;
import io.kite.frontend.parser.expressions.InputDeclaration;
import io.kite.frontend.parser.expressions.OutputDeclaration;
import io.kite.typechecker.TypeChecker;
import io.kite.typechecker.TypeError;
import io.kite.typechecker.types.DecoratorType;
import io.kite.typechecker.types.SystemType;
import io.kite.typechecker.types.Type;
import io.kite.typechecker.types.ValueType;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.typechecker.types.DecoratorType.decorator;

public class MinValueDecorator extends DecoratorChecker {
    public static final String NAME = "minValue";

    public MinValueDecorator(TypeChecker checker) {
        super(checker, NAME, decorator(List.of(ValueType.Number),
                Set.of(
                        DecoratorType.Target.INPUT,
                        DecoratorType.Target.OUTPUT
                )), Set.of(SystemType.NUMBER));
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        validateNumber(declaration, 0, 999999);

        switch (declaration.getTarget()) {
            case InputDeclaration input -> extracted(input.targetType());
            case OutputDeclaration output -> extracted(output.targetType());
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        }
        return null;
    }

    private void extracted(Type identifier) {
        if (!isAllowedOn(identifier)) {
            String message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(getName())
                    .reset()
                    .a(" is only valid for numbers but it is applied to: ")
                    .fgBlue()
                    .a(identifier.getValue())
                    .toString();
            throw new TypeError(message);
        }
    }
}
