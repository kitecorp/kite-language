package io.kite.semantics.decorators;

import io.kite.semantics.TypeChecker;
import io.kite.semantics.TypeError;
import io.kite.semantics.types.DecoratorType;
import io.kite.semantics.types.SystemType;
import io.kite.syntax.ast.expressions.AnnotationDeclaration;
import io.kite.syntax.ast.expressions.InputDeclaration;
import io.kite.syntax.ast.expressions.OutputDeclaration;
import io.kite.syntax.literals.TypeIdentifier;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static io.kite.semantics.types.DecoratorType.decorator;

public class UniqueDecorator extends DecoratorChecker {

    public static final String NAME = "unique";

    public UniqueDecorator(TypeChecker checker) {
        super(checker, NAME, decorator(List.of(), Set.of(DecoratorType.Target.INPUT)), Set.of(SystemType.ARRAY));
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        if (doesNotHaveArguments(declaration)) {
            var message = Ansi.ansi()
                    .a(printer.visit(declaration))
                    .a(" must not have any arguments")
                    .toString();
            throw new TypeError(message);
        }
        switch (declaration.getTarget()) {
            case InputDeclaration input -> isAllowedOnType(input.getType());
            case OutputDeclaration input -> isAllowedOnType(input.getType());
            default -> {
                String message = Ansi.ansi()
                        .fgYellow()
                        .a("@").a(getName())
                        .reset()
                        .a(" can only be used on inputs and outputs")
                        .toString();
                throw new TypeError(message);
            }
        }
        return null;
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

    private void isAllowedOnType(TypeIdentifier input) {
        if (input instanceof TypeIdentifier literal) {
            if (!isAllowedOn(literal)) {
                String message = Ansi.ansi()
                        .fgYellow()
                        .a("@").a(getName())
                        .reset()
                        .a(" is only valid for arrays. Applied to: ")
                        .fgBlue()
                        .a(literal.getType().getValue())
                        .toString();
                throw new TypeError(message);
            }
        }
    }


}
