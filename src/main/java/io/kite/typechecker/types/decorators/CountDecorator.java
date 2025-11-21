package io.kite.typechecker.types.decorators;

import io.kite.frontend.parse.literals.Identifier;
import io.kite.frontend.parse.literals.NumberLiteral;
import io.kite.frontend.parse.literals.StringLiteral;
import io.kite.frontend.parser.expressions.AnnotationDeclaration;
import io.kite.frontend.parser.expressions.ComponentStatement;
import io.kite.frontend.parser.expressions.ResourceStatement;
import io.kite.typechecker.TypeChecker;
import io.kite.typechecker.TypeError;
import io.kite.typechecker.types.DecoratorType;
import io.kite.typechecker.types.ValueType;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.typechecker.types.DecoratorType.decorator;

public class CountDecorator extends DecoratorChecker {

    public static final String NAME = "count";

    public CountDecorator(TypeChecker checker) {
        super(checker, NAME, decorator(List.of(ValueType.Number), Set.of(DecoratorType.Target.RESOURCE, DecoratorType.Target.COMPONENT)), Set.of());
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        switch (declaration.getValue()) {
            case Identifier identifier -> {
                var type = checker.visit(identifier);
                if (type != ValueType.Number) {
                    String message = Ansi.ansi()
                            .fgYellow()
                            .a("@").a(getName())
                            .reset()
                            .a(" only accepts numbers as arguments but it got: ")
                            .a(type.getValue())
                            .toString();
                    throw new TypeError(message);
                }
            }
            case NumberLiteral literal -> {
            }
            case StringLiteral literal -> {
                String message = Ansi.ansi()
                        .fgYellow()
                        .a("@").a(getName())
                        .reset()
                        .a(" only accepts numbers as arguments but it got: ")
                        .a(checker.getPrinter().visit(literal))
                        .toString();
                throw new TypeError(message);
            }
            case String expression -> {
                String message = Ansi.ansi()
                        .fgYellow()
                        .a("@").a(getName())
                        .reset()
                        .a(" only accepts numbers as arguments but it got: ")
                        .a(checker.getPrinter().visit(declaration.getValue()))
                        .toString();
                throw new TypeError(message);
            }
            case null -> {
                String message = Ansi.ansi()
                        .fgYellow()
                        .a("@").a(getName())
                        .reset()
                        .a(" only accepts numbers as arguments but it got: ")
                        .a(checker.getPrinter().visit(declaration.getValue()))
                        .toString();
                throw new TypeError(message);
            }
            default -> {
            }
        }
        var body = switch (declaration.getTarget()) {
            case ResourceStatement expression -> expression;
            case ComponentStatement statement -> {
                if (statement.isDefinition()) {
                    String message = Ansi.ansi()
                            .fgYellow()
                            .a("@").a(getName())
                            .reset()
                            .a(" cannnot be applied to a component definition")
                            .toString();
                    throw new TypeError(message);
                }
                yield statement;
            }
            default -> throw new TypeError("Unexpected value: " + declaration.getTarget());
        };

        return null;
    }

}
