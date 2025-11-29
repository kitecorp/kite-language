package cloud.kitelang.semantics.decorators;

import cloud.kitelang.semantics.TypeChecker;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.semantics.types.DecoratorType;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static cloud.kitelang.semantics.types.DecoratorType.decorator;

public class CloudDecorator extends DecoratorChecker {

    public static final String NAME = "cloud";

    public CloudDecorator(TypeChecker checker) {
        super(checker, NAME, decorator(List.of(), Set.of(DecoratorType.Target.SCHEMA_PROPERTY)), Set.of());
    }

    @Override
    public Object validate(AnnotationDeclaration declaration, List<Object> args) {
        if (declaration.getValue() != null || declaration.hasArgs() || declaration.getObject() != null) {
            var message = Ansi.ansi()
                    .fgYellow()
                    .a("@").a(getName())
                    .reset()
                    .a(" does not accept any arguments")
                    .toString();
            throw new TypeError(message);
        }
        return null;
    }
}
