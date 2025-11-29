package cloud.kitelang.semantics.decorators;

import cloud.kitelang.semantics.TypeChecker;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.semantics.types.DecoratorType;
import cloud.kitelang.semantics.types.ValueType;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.ast.expressions.ComponentStatement;
import cloud.kitelang.syntax.ast.expressions.ResourceStatement;
import cloud.kitelang.syntax.literals.Identifier;
import cloud.kitelang.syntax.literals.NumberLiteral;
import cloud.kitelang.syntax.literals.StringLiteral;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static cloud.kitelang.semantics.types.DecoratorType.decorator;

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
