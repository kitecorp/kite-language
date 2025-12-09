package cloud.kitelang.semantics.decorators;

import cloud.kitelang.analysis.visitors.SyntaxPrinter;
import cloud.kitelang.execution.values.ResourceRef;
import cloud.kitelang.semantics.TypeChecker;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.semantics.types.*;
import cloud.kitelang.syntax.ast.expressions.*;
import cloud.kitelang.syntax.literals.Identifier;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static cloud.kitelang.semantics.types.DecoratorType.decorator;

public class DependsOnDecorator extends DecoratorChecker {
    public static final String NAME = "dependsOn";
    private final TypeChecker checker;
    private SyntaxPrinter printer;

    public DependsOnDecorator(TypeChecker checker) {
        super(checker, NAME, decorator(
                List.of(ArrayType.ARRAY_TYPE, ResourceType.INSTANCE),
                Set.of(DecoratorType.Target.RESOURCE, DecoratorType.Target.COMPONENT)
        ), Set.of());
        this.checker = checker;
        this.printer = checker.getPrinter();
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        validateMissingArgs(declaration); // validate decorator arguments

        validateArgsType(declaration);

        return null;
    }

    private void validateArgsType(AnnotationDeclaration declaration) {
        if (declaration.getTarget() instanceof ComponentStatement statement) {
            if (statement.isDefinition()) {
                String visit = printer.visit(statement);
                String message = Ansi.ansi()
                        .fgYellow()
                        .a("@").a(getName())
                        .reset()
                        .a(" cannot be applied on a component definition: ")
                        .fgBlue()
                        .a(visit)
                        .toString();
                throw new TypeError(message);
            }
        }
        if (declaration.getArgs() instanceof ArrayExpression arrayExpression) {
            for (Expression item : arrayExpression.getItems()) {
                switch (item) {
                    case MemberExpression expression -> checkDependency((Identifier) expression.getProperty());
                    case Identifier identifier -> checkDependency(identifier);
                    case null, default -> throwErrorForInvalidArgument(item);
                }
            }
        } else if (declaration.getValue() instanceof Identifier identifier) {
            checkDependency(identifier);
        } else if (!(declaration.getValue() instanceof MemberExpression memberExpression)) {
            throwErrorForInvalidArgument(declaration);
        }
    }

    /**
     * Checks if the dependency is a Resource or a Component. Can't be anything else
     */
    private void checkDependency(Identifier item) {
        var res = checker.lookupInstance(item);
        switch (res) {
            case ResourceType _ -> {
            }
            case ComponentType component -> { // we got a component instance and definition. A dependency can't be a component definition
                if (component.isDefinition()) {
                    String visit = printer.visit(component);
                    String message = Ansi.ansi()
                            .fgYellow()
                            .a("@").a(getName())
                            .reset()
                            .a(" cannot depend on a component definition: ")
                            .fgBlue()
                            .a(visit)
                            .toString();
                    throw new TypeError(message);
                }
            }
            case AnyType anyType when anyType.getAny() instanceof ResourceRef.Pending -> {
            }
            default -> throwErrorForInvalidArgument(item);
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

    private void throwErrorForInvalidArgument(Expression typeIdentifier) {
        String visit = printer.visit(typeIdentifier);
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
