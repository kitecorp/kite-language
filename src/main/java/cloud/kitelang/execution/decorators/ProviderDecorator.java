package cloud.kitelang.execution.decorators;

import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.literals.StringLiteral;

import java.util.List;
import java.util.Set;

public class ProviderDecorator extends DecoratorInterpreter {
    private final Interpreter interpreter;

    public ProviderDecorator(Interpreter interpreter) {
        super("provider");
        this.interpreter = interpreter;
    }


    @Override
    public Object execute(AnnotationDeclaration declaration) {
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
