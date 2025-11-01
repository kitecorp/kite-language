package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.ComponentStatement;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.Parser.Expressions.ResourceStatement;
import io.kite.Runtime.Interpreter;

import java.util.HashSet;
import java.util.Set;

/**
 * Just sets the dependencies list and does nothing more.
 * Actual evaluation and cycle detection will happen in the interpreter.
 * Resources that were not evaluated yet will be added to the dependency as deffered
 */
public class DependsOnDecorator extends DecoratorInterpreter {

    private final Interpreter interpreter;

    public DependsOnDecorator(Interpreter interpreter) {
        super("dependsOn");
        this.interpreter = interpreter;
    }

    @Override
    public Object execute(AnnotationDeclaration declaration) {
        if (declaration.getValue() instanceof Expression identifier) {
            var set = Set.of(identifier);
            registerDependency(declaration, set);
            return set;
        } else if (declaration.getArgs() != null) {
            var set = new HashSet<>(declaration.getArgs().getItems());
            registerDependency(declaration, set);
            return set;
        }

        return null;
    }

    private void registerDependency(AnnotationDeclaration declaration, Set<Expression> dependencies) {
        switch (declaration.getTarget()) {
            case ResourceStatement resource -> resource.setDependencies(dependencies);
            case ComponentStatement component -> component.setDependencies(dependencies);
            default ->
                    throw new IllegalStateException("Hou cannot set dependencies on: " + declaration.getTarget().targetType().getKind().toString().toLowerCase());
        }
    }


}
