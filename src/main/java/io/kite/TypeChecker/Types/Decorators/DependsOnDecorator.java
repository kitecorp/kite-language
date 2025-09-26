package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.ArrayExpression;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.Parser.Expressions.MemberExpression;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.*;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class DependsOnDecorator extends DecoratorChecker {
    public static final String DEPENDS_ON = "dependsOn";

    public DependsOnDecorator() {
        super(DEPENDS_ON, decorator(
                List.of(ArrayType.ARRAY_TYPE, ReferenceType.Resource),
                Set.of(DecoratorType.Target.RESOURCE, DecoratorType.Target.COMPONENT)
        ), Set.of(SystemType.RESOURCE, SystemType.COMPONENT));
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        argValidation(declaration);

        if (declaration.getArgs() instanceof ArrayExpression arrayExpression) {
            for (Expression item : arrayExpression.getItems()) {
                extracted(declaration);
            }
        } else if (declaration.getValue() instanceof MemberExpression memberExpression) {
            extracted(declaration);
        }

        return null;
    }

    private void extracted(AnnotationDeclaration declaration) {
        throwTypeErrorForInvalidArgument(declaration.getTarget().targetType());
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
        } else if (declaration.getValue() == null && (declaration.getArgs() == null || declaration.getArgs().isEmpty())) {
            String message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(getName())
                    .reset()
                    .a(" expects at least one argument")
                    .toString();
            throw new TypeError(message);
        }
    }

    private void throwTypeErrorForInvalidArgument(Type typeIdentifier) {
        if (!isAllowedOn(typeIdentifier)) {
            String message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(getName())
                    .reset()
                    .a(" is only valid for resource and component references or arrays of resources and components. Applied to: ")
                    .fgBlue()
                    .a(typeIdentifier.getValue())
                    .toString();
            throw new TypeError(message);
        }
    }
}
