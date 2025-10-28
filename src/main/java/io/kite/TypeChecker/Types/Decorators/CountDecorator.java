package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parse.Literals.NumberLiteral;
import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.ComponentStatement;
import io.kite.Frontend.Parser.Expressions.ResourceStatement;
import io.kite.TypeChecker.TypeChecker;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.ValueType;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class CountDecorator extends DecoratorChecker {

    public static final String NAME = "count";
    private final TypeChecker typeChecker;

    public CountDecorator(TypeChecker typeChecker) {
        super(NAME, decorator(List.of(ValueType.Number), Set.of(DecoratorType.Target.RESOURCE, DecoratorType.Target.COMPONENT)), Set.of());
        this.typeChecker = typeChecker;
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        switch (declaration.getValue()) {
            case Identifier identifier -> {
                var type = typeChecker.visit(identifier);
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
            case NumberLiteral literal -> {}
            case StringLiteral literal -> {
                String message = Ansi.ansi()
                        .fgYellow()
                        .a("@").a(getName())
                        .reset()
                        .a(" only accepts numbers as arguments but it got: ")
                        .a(typeChecker.getPrinter().visit(literal))
                        .toString();
                throw new TypeError(message);
            }
            case String expression -> {
                String message = Ansi.ansi()
                        .fgYellow()
                        .a("@").a(getName())
                        .reset()
                        .a(" only accepts numbers as arguments but it got: ")
                        .a(typeChecker.getPrinter().visit(declaration.getValue()))
                        .toString();
                throw new TypeError(message);
            }
            case null -> {
                String message = Ansi.ansi()
                        .fgYellow()
                        .a("@").a(getName())
                        .reset()
                        .a(" only accepts numbers as arguments but it got: ")
                        .a(typeChecker.getPrinter().visit(declaration.getValue()))
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
