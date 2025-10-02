package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.Parser.Expressions.MemberExpression;
import io.kite.Frontend.Parser.Expressions.ResourceExpression;
import io.kite.Runtime.Interpreter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Just sets the dependencies list and does nothing more. Actual evaluation and cycle detection will happen in the interpreter
 */
public class DependsOnDecorator extends NumberDecorator {

    public DependsOnDecorator() {
        super("dependsOn");
    }

    @Override
    public Object execute(Interpreter interpreter, AnnotationDeclaration declaration) {
        if (declaration.getValue() instanceof MemberExpression member) {
            return registerDependency(declaration, Set.of(member));
        } else if (declaration.getArgs() != null) {
            var set = new HashSet<>(declaration.getArgs().getItems());
            registerDependency(declaration, set);
            return set;
        }

        return null;
    }

    private @NotNull Set<Expression> registerDependency(AnnotationDeclaration declaration, Set<Expression> visit) {
        var resource = (ResourceExpression) declaration.getTarget();
        resource.setDependencies(visit);
        return resource.getDependencies();
    }


}
