package cloud.kitelang.semantics.decorators;

import cloud.kitelang.semantics.TypeChecker;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.semantics.types.DecoratorType;
import cloud.kitelang.semantics.types.SystemType;
import cloud.kitelang.semantics.types.ValueType;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.ast.expressions.ComponentStatement;
import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.literals.Identifier;
import cloud.kitelang.syntax.literals.ObjectLiteral;
import cloud.kitelang.syntax.literals.StringLiteral;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;

import static cloud.kitelang.semantics.types.DecoratorType.decorator;

public class TagsDecorator extends DecoratorChecker {
    public static final String NAME = "tags";

    public TagsDecorator(TypeChecker checker) {
        super(checker, NAME, decorator(
                        List.of(ValueType.String),
                        Set.of(DecoratorType.Target.RESOURCE, DecoratorType.Target.COMPONENT)
                ), Set.of()
        );
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        var value = declaration.getValue();
        if (value == null && (declaration.getArgs() == null || declaration.getArgs().isEmpty()) && declaration.getObject() == null) {
            throwIfInvalidArgs(declaration);
        }
        if (declaration.getTarget() instanceof ComponentStatement statement) {
            if (statement.isDefinition()) {
                throwInvalidArgument(declaration, statement.getName());
            }
        }
        if (value != null) {
            validateString(declaration, value);
        } else if (declaration.hasArgs()) {
            for (Expression item : declaration.getArgs().getItems()) {
                if (item instanceof StringLiteral literal) {
                    validateString(declaration, literal);
                } else {
                    throwInvalidArgument(declaration, item);
                }
            }
        } else if (declaration.getObject() != null) {
            for (ObjectLiteral property : declaration.getObject().getProperties()) {
                if (property.getKey() instanceof StringLiteral literal) {
                    if (StringUtils.isBlank(literal.getValue())) {
                        throwInvalidArgument(declaration, literal);
                    }
                } else {
                    throwInvalidArgument(declaration, property.getKey());
                }
                if (property.getValue() instanceof StringLiteral literal) {
                    if (StringUtils.isBlank(literal.getValue())) {
                        throwInvalidArgument(declaration, literal);
                    }
                } else {
                    throwInvalidArgument(declaration, property.getValue());
                }
            }
        }
        return null;
    }

    private void validateString(AnnotationDeclaration declaration, Object value) {
        switch (value) {
            case StringLiteral literal -> {
                if (StringUtils.isBlank(literal.getValue())) {
                    throwIfInvalidArgs(declaration);
                }
            }
            case Identifier identifier -> {
                var res = checker.visit(identifier);
                if (res.getKind() != SystemType.STRING) {
                    throw new TypeError("%s has invalid argument: %s".formatted(printer.visit(declaration), printer.visit(value)));
                }
            }
            case null, default -> throwInvalidArgument(declaration, value);
        }
    }

    private void throwInvalidArgument(AnnotationDeclaration declaration, Object value) {
        throw new TypeError("%s has invalid argument: %s".formatted(printer.visit(declaration), printer.visit(value)));
    }

    private void throwIfInvalidArgs(AnnotationDeclaration declaration) {
        throw new TypeError("%s must have a non-empty string as argument or an array of strings or an object".formatted(printer.visit(declaration)));
    }
}
