package cloud.kitelang.execution.values;

import cloud.kitelang.syntax.ast.expressions.Expression;
import cloud.kitelang.syntax.ast.expressions.ResourceStatement;
import cloud.kitelang.syntax.literals.Identifier;

import java.util.Set;

/**
 * Template for resources whose count depends on @cloud properties.
 * Created during plan when @count evaluates to a DeferredValue.
 * Instantiated during apply after dependencies are created in the cloud.
 *
 * <p>Example:
 * <pre>
 * resource Subnet subnet {
 *     vpcId = vpc.vpcId
 *     cidrBlock = "10.0.0.0/24"
 * }
 *
 * @count(length([subnet]))  // If subnet references @cloud properties
 * resource S3Bucket bucket {
 *     bucket = "my-bucket-$count"
 * }
 * </pre>
 *
 * <p>When @count cannot be evaluated at plan time (because it depends on
 * cloud-managed properties), a DeferredResourceTemplate is created instead
 * of actual resources. During apply, after the dependencies are created
 * and their cloud properties are available, the template is instantiated.
 *
 * @param templateName      the base name for resources (e.g., "bucket")
 * @param countExpression   the expression to re-evaluate during apply
 * @param resourceStatement the resource statement template to instantiate
 * @param dependencies      resource names this template depends on
 * @param resourceType      the type identifier for the resource (e.g., "S3Bucket")
 * @param deferredValue     the deferred value that caused this template to be created
 */
public record DeferredResourceTemplate(
        String templateName,
        Expression countExpression,
        ResourceStatement resourceStatement,
        Set<String> dependencies,
        Identifier resourceType,
        DeferredValue deferredValue
) {
    /**
     * Check if all dependencies have been resolved (created in cloud).
     *
     * @param createdResources map of resource names that have been created
     * @return true if all dependencies are in the created resources map
     */
    public boolean allDependenciesResolved(Set<String> createdResources) {
        return createdResources.containsAll(dependencies);
    }

    /**
     * Get a description for plan output.
     *
     * @return human-readable description of why this resource is deferred
     */
    public String getDeferredReason() {
        return "count depends on " + deferredValue.dependencyName() + "." + deferredValue.propertyPath();
    }

    @Override
    public String toString() {
        return templateName + "[?] (deferred - " + getDeferredReason() + ")";
    }
}
