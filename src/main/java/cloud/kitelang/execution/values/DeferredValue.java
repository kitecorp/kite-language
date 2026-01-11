package cloud.kitelang.execution.values;

/**
 * Represents a deferred reference to a cloud-managed property.
 * <p>
 * When a resource property references a {@code @cloud} property of another resource,
 * the value will be null during interpretation because cloud-managed properties are
 * only assigned by the cloud provider during apply.
 * <p>
 * This record stores the reference so it can be resolved during apply, after the
 * dependent resource has been created and its cloud-managed property is populated.
 * <p>
 * Example:
 * <pre>
 * resource Vpc example {
 *     cidrBlock = "10.0.0.0/24"
 * }
 * resource Subnet subnet {
 *     vpcId = example.vpcId  // example.vpcId is @cloud, so this becomes DeferredValue("example", "vpcId")
 * }
 * </pre>
 *
 * @param dependencyName the name of the resource being referenced (e.g., "example")
 * @param propertyPath   the property path to resolve (e.g., "vpcId")
 * @see SchemaValue#isCloudProperty(String)
 */
public record DeferredValue(
        String dependencyName,
        String propertyPath
) {
    @Override
    public String toString() {
        return "${" + dependencyName + "." + propertyPath + "}";
    }
}
