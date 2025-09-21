package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parse.Literals.ArrayTypeIdentifier;
import io.kite.Frontend.Parse.Literals.NumberLiteral;
import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.ArrayExpression;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.*;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class AllowedDecorator extends DecoratorCallable {
    public static final String ALLOWED = "allowed";

    public AllowedDecorator() {
        super(ALLOWED, decorator(List.of(ArrayType.ARRAY_TYPE, ObjectType.INSTANCE, ValueType.Number, ValueType.String), Set.of(
                DecoratorType.Target.INPUT
        )));
    }

    @Override
    protected boolean isAllowedOn(TypeIdentifier literal) {
        return literal instanceof ArrayTypeIdentifier
               || literal.getType().getKind() == SystemType.STRING
               || literal.getType().getKind() == SystemType.OBJECT
               || literal.getType().getKind() == SystemType.NUMBER;
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        argValidation(declaration);

        if (declaration.getArgs() instanceof ArrayExpression arrayExpression) {
            for (Expression item : arrayExpression.getItems()) {
                switch (item) {
                    case StringLiteral literal -> {
                        if (declaration.getTarget().targetType() != ValueType.String) {
                            throwTypeErrorForInvalidArgument(declaration);
                        }
                    }
                    case NumberLiteral literal -> {
                        if (declaration.getTarget().targetType() != ValueType.Number) {
                            throwTypeErrorForInvalidArgument(declaration);
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + item);
                }
            }
        }

        switch (declaration.getTarget()) {
            case InputDeclaration input -> throwTypeErrorForInvalidArgument(input.getType());
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        }
        return null;
    }

    private void argValidation(AnnotationDeclaration declaration) {
         if (declaration.getObject() != null) {
            String message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(getName())
                    .reset()
                    .a(" does not accept objects as arguments")
                    .toString();
            throw new TypeError(message);
        } else if (declaration.getValue() != null) {
            String message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(getName())
                    .reset()
                    .a(" does not accept literals as arguments")
                    .toString();
            throw new TypeError(message);
        } else if (declaration.getArgs() == null || declaration.getArgs().isEmpty()) {
            String message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(getName())
                    .reset()
                    .a(" does not accept empty array arguments")
                    .toString();
            throw new TypeError(message);
        }
    }

    private void throwTypeErrorForInvalidArgument(AnnotationDeclaration declaration) {
        String message = Ansi.ansi()
                .fgYellow()
                .a("@").a(getName())
                .reset()
                .a(" should have string type as argument because ")
                .fgMagenta()
                .a(declaration.getTarget())
                .reset()
                .a(" is of type ")
                .fgBlue()
                .a(declaration.getTarget().targetType())
                .toString();
        throw new TypeError(message);
    }

    private void throwTypeErrorForInvalidArgument(TypeIdentifier input) {
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
