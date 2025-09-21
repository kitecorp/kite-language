package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Frontend.Parser.Expressions.OutputDeclaration;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.*;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class MaxValueDecorator extends DecoratorCallable {
    public static final String MAX_VALUE = "maxValue";

    public MaxValueDecorator() {
        super(MAX_VALUE, decorator(List.of(ValueType.Number), Set.of(
                DecoratorType.Target.INPUT,
                DecoratorType.Target.OUTPUT
        )));
    }

    @Override
    protected boolean isAllowedOn(TypeIdentifier literal) {
        Type type = literal.getType();
        SystemType kind = type.getKind();
        return kind == SystemType.NUMBER;
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
                    .a(" is only valid for numbers but it is applied to: ")
                    .fgBlue()
                    .a(identifier.getType().getValue())
                    .toString();
            throw new TypeError(message);
        }
    }
}
