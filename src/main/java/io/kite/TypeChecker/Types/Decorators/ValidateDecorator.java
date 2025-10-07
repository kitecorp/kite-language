package io.kite.TypeChecker.Types.Decorators;

import io.kite.Frontend.Parse.Literals.StringLiteral;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.Expressions.Expression;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.DecoratorType;
import io.kite.TypeChecker.Types.SystemType;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.TypeChecker.Types.DecoratorType.decorator;

public class ValidateDecorator extends DecoratorChecker {
    public static final String NAME = "validate";

    public ValidateDecorator() {
        super(NAME, decorator(List.of(),
                        Set.of(DecoratorType.Target.INPUT, DecoratorType.Target.OUTPUT)
                ),
                Set.of(SystemType.ARRAY, SystemType.STRING)
        );
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        var namedArgs = declaration.getNamedArgs();

        if (namedArgs == null || namedArgs.isEmpty()) {
            throw new TypeError("Missing %s arguments!".formatted(validateFormatting()));
        }
        Expression regex = namedArgs.get("regex");
        if (regex == null) {
            throw new TypeError("regex argument is required for %s".formatted(validateFormatting()));
        } else if (!(regex instanceof StringLiteral)) {
            throw new TypeError("regex argument must be a string literal for %s".formatted(validateFormatting()));
        }
        if (!isAllowedOn(declaration.getTarget().targetType())) {
            throw new TypeError("%s is not allowed on %s".formatted(validateFormatting(), declaration.getTarget().targetType().getValue()));
        }
        return null;
    }


    @Override
    public boolean hasArguments(AnnotationDeclaration declaration) {
        return declaration.getValue() != null
               || declaration.getObject() != null
               || declaration.getArgs() != null && !declaration.getArgs().isEmpty()
               || declaration.getNamedArgs() != null && declaration.getNamedArgs().isEmpty();
    }

    private String validateFormatting() {
        return Ansi.ansi().fgYellow().a("@").a(getName()).reset().toString();
    }


//    @Override
//    public boolean validateAfterInit(AnnotationDeclaration declaration) {
//        switch (declaration.getTarget()){
//            case InputDeclaration inputDeclaration -> {
//                if (!inputDeclaration.hasInit()) {
//                    throw new TypeError("Missing init for unique input");
//                }
//            }
//            case OutputDeclaration outputDeclaration -> {
//                if (!outputDeclaration.hasInit()) {
//                    throw new TypeError("Missing init for unique output");
//                }
//            }
//            default -> throw new IllegalStateException("Unexpected value: " + declaration.getTarget());
//        }
//        return true;
//    }


}
