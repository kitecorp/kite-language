package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parse.Literals.ObjectLiteral;
import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.TypeChecker.TypeChecker;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.ValueType;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class TagsDecorator extends DecoratorChecker {
    public static final String NAME = "tags";
    private final TypeChecker typeChecker;

    public TagsDecorator(TypeChecker typeChecker) {
        super(NAME, decorator(
                        List.of(ValueType.String),
                        Set.of(DecoratorType.Target.RESOURCE, DecoratorType.Target.COMPONENT)
                ), Set.of()
        );
        this.typeChecker = typeChecker;
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        var value = declaration.getValue();
        if (value == null && (declaration.getArgs() != null && declaration.getArgs().isEmpty()) && declaration.getObject() == null) {
            throwIfInvalidArgs(declaration);
        }

        if (value != null) {
            if (value instanceof StringLiteral literal) {
                if (literal.getValue().isEmpty()) {
                    throwIfInvalidArgs(declaration);
                }
            } else {
                throwInvalidArgument(declaration, value);
            }
        } else if (declaration.getArgs() != null && !declaration.getArgs().isEmpty()) {
            for (Expression item : declaration.getArgs().getItems()) {
                if (!(item instanceof StringLiteral literal)) {
                    throwInvalidArgument(declaration, item);
                }
            }
        } else if (declaration.getObject() != null) {
            for (ObjectLiteral property : declaration.getObject().getProperties()) {
                if (!(property.getKey() instanceof StringLiteral literal)) {
                    throwInvalidArgument(declaration, property);
                }
            }
        }
        return null;
    }

    private void throwInvalidArgument(AnnotationDeclaration declaration, Object value) {
        throw new TypeError("%s has invalid argument `%s`".formatted(typeChecker.getPrinter().visit(declaration), typeChecker.getPrinter().visit(value)));
    }

    private void throwIfInvalidArgs(AnnotationDeclaration declaration) {
        throw new TypeError("%s must have a non-empty string as argument or an array of strings or an object".formatted(typeChecker.getPrinter().visit(declaration)));
    }
}
