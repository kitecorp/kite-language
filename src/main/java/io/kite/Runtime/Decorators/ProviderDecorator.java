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
        if (declaration.getTarget() instanceof ProviderSupport providerSupport) {
            if (declaration.getArgs() != null && !declaration.getArgs().getItems().isEmpty()) {
                var providers = (List<String>) interpreter.visit(declaration.getArgs());
                providerSupport.setProviders(Set.copyOf(providers));
            } else if (declaration.getValue() != null && declaration.getValue() instanceof StringLiteral literal) {
                var provider = (String) interpreter.visit(literal);
                providerSupport.addProvider(provider);
            }
//            supportsProviders.addProvider();
        } else {
            throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
        }
        return null;
    }


}
