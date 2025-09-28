package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.Frontend.Parser.Expressions.MemberExpression;
import io.kite.Frontend.Parser.Expressions.ResourceExpression;
import io.kite.Runtime.Interpreter;
import io.kite.Visitors.SyntaxPrinter;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class DependsOnDecorator extends NumberDecorator {
    private SyntaxPrinter printer = new SyntaxPrinter();

    public DependsOnDecorator() {
        super("dependsOn");
    }

    @Override
    public Object execute(Interpreter interpreter, AnnotationDeclaration declaration) {
        if (declaration.getValue() instanceof MemberExpression member) {
            return registerDependency(declaration, Set.of(printer.visit(member)));
        } else if (declaration.getArgs() != null) {
            var set = new HashSet<String>();
            for (Expression item : declaration.getArgs().getItems()) {
                if (item instanceof MemberExpression memberExpression) {
                    set.add(printer.visit(memberExpression));
                }
            }
            registerDependency(declaration, set);
            return set;
        }

        return null;
    }

    private @NotNull Set<String> registerDependency(AnnotationDeclaration declaration, Set<String> visit) {
        var resource = (ResourceExpression) declaration.getTarget();
        resource.setDependencies(visit);
        return resource.getDependencies();
    }


}
