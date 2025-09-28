package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.MemberExpression;
import io.kite.Frontend.Parser.Expressions.ResourceExpression;
import io.kite.Runtime.Interpreter;
import io.kite.Visitors.SyntaxPrinter;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DependsOnDecorator extends NumberDecorator {
    private SyntaxPrinter printer = new SyntaxPrinter();

    public DependsOnDecorator() {
        super("dependsOn");
    }

    @Override
    public Object execute(Interpreter interpreter, AnnotationDeclaration declaration) {
        if (declaration.getValue() instanceof MemberExpression member) {
            return registerDependency(declaration, member);
        } else {
        }

        return null;
    }

    private @NotNull Set<String> registerDependency(AnnotationDeclaration declaration, MemberExpression member) {
        var resource = (ResourceExpression) declaration.getTarget();
        resource.setDependencies(Set.of(printer.visit(member)));
        return resource.getDependencies();
    }


}
