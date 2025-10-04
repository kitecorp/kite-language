package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parse.Literals.NumberLiteral;
import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.ArrayExpression;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Frontend.annotations.Annotatable;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.*;
import io.kite.Visitors.SyntaxPrinter;
import org.fusesource.jansi.Ansi;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class AllowedDecorator extends DecoratorChecker {
    public static final String ALLOWED = "allowed";
    private SyntaxPrinter syntaxPrinter = new SyntaxPrinter();

    public AllowedDecorator() {
        super(ALLOWED, decorator(List.of(ArrayType.ARRAY_TYPE, ObjectType.INSTANCE, ValueType.Number, ValueType.String),
                        Set.of(DecoratorType.Target.INPUT)),
                Set.of(SystemType.STRING, SystemType.OBJECT, SystemType.NUMBER, SystemType.ARRAY));
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        argValidation(declaration);

        if (declaration.getArgs() instanceof ArrayExpression arrayExpression) {
            for (Expression item : arrayExpression.getItems()) {
                switch (item) {
                    case StringLiteral literal -> expectTargetType(declaration, ValueType.String, AnyType.INSTANCE);
                    case NumberLiteral literal -> expectTargetType(declaration, ValueType.Number, AnyType.INSTANCE);
                    default -> throw new IllegalStateException("Unexpected value: " + item);
                }
            }
        }

        if (declaration.getTarget() instanceof InputDeclaration input) {
            throwTypeErrorForInvalidArgument(input);
        } else {
            throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        }
        return null;
    }

    private void expectTargetType(AnnotationDeclaration declaration, Type... string) {
        var types = new HashSet<>(Set.of(string));
        types.add(AnyType.INSTANCE);
        if (!types.contains(declaration.getTarget().targetType())) {
            throwTypeErrorForInvalidArgument(declaration, types);
        }
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

    private void throwTypeErrorForInvalidArgument(AnnotationDeclaration declaration, Set<Type> types) {
        Annotatable target = declaration.getTarget();
        var string = switch (target) {
            case InputDeclaration input -> syntaxPrinter.visit(input);
            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        };
        String message = Ansi.ansi()
                .fgYellow()
                .a("@").a(getName())
                .reset()
                .a(" should have %s type as argument because ".formatted(types))
                .fgMagenta()
                .a(string)
                .reset()
                .a(" is of type ")
                .fgBlue()
                .a(target.targetType().getValue())
                .toString();
        throw new TypeError(message);
    }

    private void throwTypeErrorForInvalidArgument(InputDeclaration input) {
        if (input.getType() instanceof TypeIdentifier literal) {
            if (!isAllowedOn(literal)) {
                String message = Ansi.ansi()
                        .fgYellow()
                        .a("@").a(getName())
                        .reset()
                        .a(" is only valid for `strings` and `arrays`. Applied to ")
                        .fgBlue()
                        .a(literal.getType().getValue())
                        .fgDefault()
                        .a(" in expression: ")
                        .a(syntaxPrinter.visit(input))
                        .toString();
                throw new TypeError(message);
            }
        }
    }
}
