package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.TypeChecker.TypeChecker;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.ValueType;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class ProviderDecorator extends DecoratorChecker {
    public static final String NAME = "provider";
    private final TypeChecker typeChecker;

    public ProviderDecorator(TypeChecker typeChecker) {
        super(NAME, decorator(List.of(ValueType.String),
                        Set.of(DecoratorType.Target.RESOURCE, DecoratorType.Target.COMPONENT)
                ), Set.of()
        );
        this.typeChecker = typeChecker;
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        var value = declaration.getValue();
        if (value !=null) {
            if (value instanceof StringLiteral literal && literal.getValue().isEmpty()) {
                throw new TypeError("@provider must have a non-empty string as argument or an array of strings");
            } else {
                throw new TypeError("Invalid argument in  " + typeChecker.getPrinter().visit(declaration));
            }
        } else if (declaration.getArgs() != null && !declaration.getArgs().isEmpty()) {
            for (Expression item : declaration.getArgs().getItems()) {
                if (!(item instanceof StringLiteral literal)) {
                    throw new TypeError("Invalid argument in  " + typeChecker.getPrinter().visit(declaration));
                }
            }
        }


        return null;
    }


    @Override
    public boolean hasArguments(AnnotationDeclaration declaration) {
        return declaration.getValue() == null
               && declaration.getArgs() == null
               && declaration.getArgs().isEmpty();
    }

}
