package cloud.kitelang.semantics.decorators;

import cloud.kitelang.semantics.TypeChecker;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.semantics.types.DecoratorType;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.literals.BooleanLiteral;
import cloud.kitelang.syntax.literals.Identifier;
import org.fusesource.jansi.Ansi;

import java.util.List;
import java.util.Set;

import static cloud.kitelang.semantics.types.DecoratorType.decorator;

/**
 * Decorator for marking schema properties as cloud-generated.
 *
 * Properties marked with @cloud are set by the cloud provider after apply,
 * not by the user. Examples include ARNs, IDs, endpoints, etc.
 *
 * Usage:
 * <pre>
 * schema aws_instance {
 *     string name                      // Regular property - user must set
 *     @cloud string arn                // Cloud-generated, not importable
 *     @cloud(importable) string id     // Cloud-generated, importable
 *     @cloud(importable=true) string publicIp  // Same as @cloud(importable)
 *     @cloud(importable=false) string privateIp // Same as @cloud
 * }
 * </pre>
 *
 * The `importable` argument indicates whether the property can be used
 * to import existing resources (e.g., resource IDs that can identify
 * a resource for import operations).
 */
public class CloudDecorator extends DecoratorChecker {

    public static final String NAME = "cloud";
    private static final String IMPORTABLE_ARG = "importable";

    public CloudDecorator(TypeChecker checker) {
        super(checker, NAME, decorator(DecoratorType.Target.SCHEMA_PROPERTY), Set.of());
    }

    @Override
    protected Object validate(AnnotationDeclaration declaration, List<Object> args) {
        // Check for invalid argument types
        if (declaration.getObject() != null) {
            throw invalidArgumentError("object arguments are not allowed");
        }
        if (declaration.getArgs() != null && !declaration.getArgs().isEmpty()) {
            throw invalidArgumentError("array arguments are not allowed");
        }

        // Handle different valid forms:
        // 1. @cloud - no arguments (importable=false)
        // 2. @cloud(importable) - shorthand for importable=true
        // 3. @cloud(importable=true) or @cloud(importable: true)
        // 4. @cloud(importable=false) or @cloud(importable: false)

        var value = declaration.getValue();
        var namedArgs = declaration.getNamedArgs();

        if (value != null) {
            // Form: @cloud(importable) - value is an identifier
            if (!(value instanceof Identifier id)) {
                throw invalidArgumentError("expected 'importable' identifier or named argument");
            }
            if (!IMPORTABLE_ARG.equals(id.string())) {
                throw invalidArgumentError("unknown argument '%s', expected 'importable'".formatted(id.string()));
            }
            // Valid: @cloud(importable) means importable=true
            return true;
        }

        if (namedArgs != null && !namedArgs.isEmpty()) {
            // Form: @cloud(importable=true) or @cloud(importable=false)
            if (namedArgs.size() != 1 || !namedArgs.containsKey(IMPORTABLE_ARG)) {
                var keys = String.join(", ", namedArgs.keySet());
                throw invalidArgumentError("unknown argument(s) '%s', expected 'importable'".formatted(keys));
            }

            var importableValue = namedArgs.get(IMPORTABLE_ARG);
            if (!(importableValue instanceof BooleanLiteral)) {
                throw invalidArgumentError("'importable' must be a boolean (true or false)");
            }

           return ((BooleanLiteral) importableValue).isValue();
        }

        // Form: @cloud - no arguments (importable=false)
        return false;
    }

    private TypeError invalidArgumentError(String detail) {
        var message = Ansi.ansi()
                .fgYellow().a("@cloud").reset()
                .a(": ").a(detail)
                .toString();
        return new TypeError(message);
    }
}