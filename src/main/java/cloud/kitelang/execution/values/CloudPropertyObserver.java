package cloud.kitelang.execution.values;

import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.execution.decorators.DecoratorInterpreter;
import cloud.kitelang.syntax.ast.expressions.AnnotationDeclaration;
import cloud.kitelang.syntax.ast.expressions.ResourceStatement;
import cloud.kitelang.syntax.literals.Identifier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Wraps a decorator expression that depends on cloud properties.
 * When cloud values become available, re-evaluates the expression.
 *
 * <p>This enables transparent deferred handling - decorators don't need
 * to know about DeferredValue. When the value is needed but depends on
 * a @cloud property, a CloudPropertyObserver is registered instead.
 * After apply creates the dependency, the observer is notified and
 * re-evaluates the decorator with the now-resolved value.
 *
 * <h2>Example:</h2>
 * <pre>
 * // Plan phase:
 * @count(vpc.subnetCount)  // subnetCount is @cloud
 * resource Subnet subnet { ... }
 *
 * // vpc.subnetCount evaluates to DeferredValue
 * // → CloudPropertyObserver created with:
 * //   - declaration: the @count annotation
 * //   - decorator: the CountDecorator
 * //   - dependency: DeferredValue("vpc", "subnetCount")
 *
 * // Apply phase (after vpc created):
 * // → observer.reEvaluate(interpreter)
 * // → vpc.subnetCount now evaluates to 3
 * // → CountDecorator creates subnet[0], subnet[1], subnet[2]
 * </pre>
 *
 * @see cloud.kitelang.execution.CloudObservable
 */
@Slf4j
@Getter
public class CloudPropertyObserver {
    private final AnnotationDeclaration declaration;
    private final DeferredValue dependency;
    private final DecoratorInterpreter decorator;
    private final String templateName;

    public CloudPropertyObserver(
            AnnotationDeclaration declaration,
            DeferredValue dependency,
            DecoratorInterpreter decorator) {
        this.declaration = declaration;
        this.dependency = dependency;
        this.decorator = decorator;
        this.templateName = extractTemplateName(declaration);
    }

    /**
     * Re-evaluate the decorator now that cloud values are available.
     * The expression that previously returned DeferredValue will now
     * resolve to the actual cloud value.
     *
     * @param interpreter the interpreter with cloud values injected
     * @return list of ResourceValues created by re-evaluation
     */
    public List<ResourceValue> reEvaluate(Interpreter interpreter) {
        log.debug("Re-evaluating decorator '{}' for template '{}'",
                decorator.getName(), templateName);

        // Track which resources exist before re-evaluation
        var existingNames = interpreter.getInstances().keySet()
                .stream()
                .collect(Collectors.toSet());

        // Execute the decorator - it will now get the resolved value
        decorator.execute(declaration);

        // Collect newly created resources
        var newResources = interpreter.getInstances().values().stream()
                .filter(rv -> !existingNames.contains(rv.getName()))
                .filter(rv -> rv.getName().startsWith(templateName))
                .toList();

        log.debug("Re-evaluation created {} new resource(s)", newResources.size());
        return newResources;
    }

    /**
     * Get the resource name this observer depends on.
     *
     * @return the dependency resource name
     */
    public String getDependencyName() {
        return dependency.dependencyName();
    }

    /**
     * Get the property path within the dependency.
     *
     * @return the property path (e.g., "subnetCount")
     */
    public String getPropertyPath() {
        return dependency.propertyPath();
    }

    /**
     * Extract the template name from the declaration target.
     */
    private String extractTemplateName(AnnotationDeclaration declaration) {
        if (declaration.getTarget() instanceof ResourceStatement rs) {
            var name = rs.getName();
            if (name instanceof Identifier id) {
                return id.string();
            }
            return name != null ? name.toString() : "unknown";
        }
        return "unknown";
    }

    @Override
    public String toString() {
        return "CloudPropertyObserver{" +
                "template='" + templateName + '\'' +
                ", depends=" + dependency.dependencyName() + "." + dependency.propertyPath() +
                ", decorator=" + decorator.getName() +
                '}';
    }
}
