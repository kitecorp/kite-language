package io.kite.semantics.decorators;

import io.kite.analysis.visitors.SyntaxPrinter;
import io.kite.semantics.TypeChecker;
import io.kite.semantics.TypeError;
import io.kite.semantics.types.*;
import io.kite.syntax.annotations.Annotatable;
import io.kite.syntax.ast.expressions.*;
import io.kite.syntax.literals.BooleanLiteral;
import io.kite.syntax.literals.NumberLiteral;
import io.kite.syntax.literals.StringLiteral;
import io.kite.syntax.literals.TypeIdentifier;
import org.fusesource.jansi.Ansi;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.kite.semantics.types.DecoratorType.decorator;

public class AllowedDecorator extends DecoratorChecker {
    public static final String NAME = "allowed";
    private SyntaxPrinter syntaxPrinter = new SyntaxPrinter();

    public AllowedDecorator(TypeChecker checker) {
        super(checker, NAME, decorator(
                        List.of(ArrayType.ARRAY_TYPE,
                                ObjectType.INSTANCE,
                                ValueType.Number,
                                ValueType.String
                        ),
                        Set.of(DecoratorType.Target.INPUT)),
                Set.of(SystemType.STRING,
                        SystemType.OBJECT,
                        SystemType.NUMBER,
                        SystemType.ARRAY));
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        argValidation(declaration);

        if (declaration.getArgs() instanceof ArrayExpression arrayExpression) {
            for (Expression item : arrayExpression.getItems()) {
                switch (item) {
                    case StringLiteral literal -> expectTargetType(declaration, ValueType.String);
                    case NumberLiteral literal -> expectTargetType(declaration, ValueType.Number);
                    case BooleanLiteral literal -> expectTargetType(declaration, ValueType.Boolean);
                    case ObjectExpression literal -> expectTargetType(declaration, ObjectType.INSTANCE);
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
//        types.add(AnyType.INSTANCE);
        if (!types.contains(declaration.getTarget().targetType())) {
            throwTypeErrorForInvalidArgument(declaration, types.stream().map(Type::getValue).collect(Collectors.toSet()));
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
        } else if (declaration.getArgs().getItems().size() > 256) {
            String message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(getName())
                    .reset()
                    .a(" does not accept more than 256 array arguments")
                    .toString();
            throw new TypeError(message);
        }
    }

    private void throwTypeErrorForInvalidArgument(AnnotationDeclaration declaration, Set<String> types) {
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
                .fgDefault()
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
                        .fgDefault()
                        .toString();
                throw new TypeError(message);
            }
        }
    }
}
