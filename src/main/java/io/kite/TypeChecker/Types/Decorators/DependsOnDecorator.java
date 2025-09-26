package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.ArrayExpression;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.Parser.Expressions.MemberExpression;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.ArrayType;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.ReferenceType;
import io.kite.Visitors.SyntaxPrinter;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class DependsOnDecorator extends DecoratorChecker {
    public static final String DEPENDS_ON = "dependsOn";
    private SyntaxPrinter syntaxPrinter = new SyntaxPrinter();

    public DependsOnDecorator() {
        super(DEPENDS_ON, decorator(
                List.of(ArrayType.ARRAY_TYPE, ReferenceType.Resource),
                Set.of(DecoratorType.Target.RESOURCE, DecoratorType.Target.COMPONENT)
        ), Set.of());
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        validateMissingArgs(declaration); // validate decorator arguments

        validateArgsType(declaration);

        return null;
    }

    private void validateArgsType(AnnotationDeclaration declaration) {
        if (declaration.getArgs() instanceof ArrayExpression arrayExpression) {
            for (Expression item : arrayExpression.getItems()) {
                if (!(item instanceof MemberExpression memberExpression)) {
                    throwErrorForInvalidArgument(declaration);
                }
            }
        } else if (!(declaration.getValue() instanceof MemberExpression memberExpression)) {
            throwErrorForInvalidArgument(declaration);
        }
    }

    private void validateMissingArgs(AnnotationDeclaration declaration) {
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

    private void throwErrorForInvalidArgument(AnnotationDeclaration typeIdentifier) {
        String visit = syntaxPrinter.visit(typeIdentifier.getValue()).toString();
        String message = Ansi.ansi()
                .fgYellow()
                .a("@").a(getName())
                .reset()
                .a(" must reference a resource or a component but it references: ")
                .fgBlue()
                .a(visit)
                .toString();
        throw new TypeError(message);
    }
}
