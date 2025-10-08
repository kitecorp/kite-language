package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Runtime.Interpreter;

import java.util.List;
import java.util.Set;

public class ProviderDecorator extends DecoratorInterpreter {
    public ProviderDecorator() {
        super("provider");
    }


    @Override
    public Object execute(Interpreter interpreter, AnnotationDeclaration declaration) {
        if (declaration.getTarget() instanceof SupportsProviders supportsProviders) {
            if (declaration.getArgs() != null && !declaration.getArgs().getItems().isEmpty()) {
                var providers = (List<String>) interpreter.visit(declaration.getArgs());
                supportsProviders.setProviders(Set.copyOf(providers));
            } else if (declaration.getValue() != null && declaration.getValue() instanceof StringLiteral literal) {
                var provider = (String) interpreter.visit(literal);
                supportsProviders.addProvider(provider);
            }
//            supportsProviders.addProvider();
        } else {
            throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        }
        return null;
    }


}
