package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parse.Literals.Identifier;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.ComponentStatement;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.Parser.Expressions.ResourceStatement;
import io.kite.Runtime.Interpreter;
import io.kite.Runtime.Values.ResourceValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Just sets the dependencies list and does nothing more. Actual evaluation and cycle detection will happen in the interpreter
 */
public class DependsOnDecorator extends DecoratorInterpreter {

    private final Interpreter interpreter;

    public DependsOnDecorator(Interpreter interpreter) {
        super("dependsOn");
        this.interpreter = interpreter;
    }

    @Override
    public Object execute(AnnotationDeclaration declaration) {
        if (declaration.getValue() instanceof Identifier identifier) {
            var dependency = interpreter.visit(identifier);
            return switch (dependency) {
                case ResourceValue _, ComponentStatement _ -> registerDependency(declaration, Set.of(identifier));
                case null, default ->
                        throw new IllegalStateException("A `%s` can only depend on other resources or components ".formatted(interpreter.getPrinter().visit(declaration.getTarget().getTarget().name().toLowerCase())));
            };
        } else if (declaration.getArgs() != null) {
            var set = new HashSet<>(declaration.getArgs().getItems());
            registerDependency(declaration, set);
            return set;
        }

        return null;
    }

    private @NotNull Set<Expression> registerDependency(AnnotationDeclaration declaration, Set<Expression> visit) {
        var resource = (ResourceStatement) declaration.getTarget();
        resource.setDependencies(visit);
        return resource.getDependencies();
    }


}
