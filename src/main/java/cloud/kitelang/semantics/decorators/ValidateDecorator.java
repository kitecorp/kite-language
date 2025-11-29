package cloud.kitelang.semantics.decorators;

import cloud.kitelang.semantics.TypeChecker;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.semantics.types.DecoratorType;
import cloud.kitelang.semantics.types.SystemType;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.literals.StringLiteral;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static cloud.kitelang.semantics.types.DecoratorType.decorator;

public class ValidateDecorator extends DecoratorChecker {
    public static final String NAME = "validate";

    public ValidateDecorator(TypeChecker checker) {
        super(checker, NAME, decorator(List.of(),
                        Set.of(DecoratorType.Target.INPUT, DecoratorType.Target.OUTPUT)
                ),
                Set.of(SystemType.ARRAY, SystemType.STRING)
        );
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        if (declaration.getValue() != null) {
            var message = Ansi.ansi()
                    .a(printer.visit(declaration))
                    .a(" can only have named arguments")
                    .toString();
            throw new TypeError(message);
        } else if (doesNotHaveArguments(declaration)) {
            var message = Ansi.ansi()
                    .a(printer.visit(declaration))
                    .a(" is missing arguments")
                    .toString();
            throw new TypeError(message);
        }

        var namedArgs = declaration.getNamedArgs();

        if (namedArgs == null || namedArgs.isEmpty()) {
            throw new TypeError("%s is missing arguments!".formatted(printer.visit(declaration)));
        }
        Expression regex = namedArgs.get("regex");
        Expression preset = namedArgs.get("preset");
        if (regex == null && preset == null) {
            throw new TypeError("%s regex argument or preset argument is required".formatted(printer.visit(declaration)));
        } else if (!(regex instanceof StringLiteral) && !(preset instanceof StringLiteral)) {
            throw new TypeError("%s regex argument must be a string literal".formatted(printer.visit(declaration)));
        }
        if (!isAllowedOn(declaration.getTarget().targetType())) {
            throw new TypeError("%s is not allowed on %s".formatted(printer.visit(declaration), declaration.getTarget().targetType().getValue()));
        }
        return null;
    }


    @Override
    public boolean doesNotHaveArguments(AnnotationDeclaration declaration) {
        return declaration.getValue() != null
               || declaration.getObject() != null
               || declaration.getArgs() != null && !declaration.getArgs().isEmpty()
               || declaration.getNamedArgs() != null && declaration.getNamedArgs().isEmpty();
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
