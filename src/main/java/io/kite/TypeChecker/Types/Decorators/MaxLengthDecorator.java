package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parse.Literals.ArrayTypeIdentifier;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Frontend.Parser.Expressions.OutputDeclaration;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.*;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class MaxLengthDecorator extends DecoratorCallable {
    public static final String MAX_LENGTH = "maxLength";

    public MaxLengthDecorator() {
        super(MAX_LENGTH, decorator(List.of(ValueType.Number), Set.of(
                DecoratorType.Target.INPUT,
                DecoratorType.Target.OUTPUT
        )));
    }

    private static boolean isAllowedOn(TypeIdentifier literal) {
        Type type = literal.getType();
        SystemType kind = type.getKind();
        return kind == SystemType.STRING || literal instanceof ArrayTypeIdentifier;
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        validateNumber(declaration,0,999999);

        switch (declaration.getTarget()) {
            case InputDeclaration input -> extracted(input.getType());
            case OutputDeclaration output -> extracted(output.getType());
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        }
        return null;
    }

    private static void extracted(TypeIdentifier input) {
        if (input instanceof TypeIdentifier literal) {
            if (!isAllowedOn(literal)) {
                throw new TypeError("@maxLength is only valid for strings and arrays. Applied to: " + literal.getType());
            }
        }
    }
}
