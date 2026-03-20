package cloud.kitelang.execution.values;

/**
 * Represents a deferred function call on a cloud-managed property.
 * <p>
 * When a function like {@code length()} receives a {@link DeferredValue},
 * it returns this record to propagate the deferral. The actual function
 * call will be executed during apply when the value is available.
 * <p>
 * Example:
 * <pre>
 * resource Subnet subnet { }
 *
 * @count(length(subnet.arn))  // subnet.arn is @cloud string
 * resource S3Bucket bucket {
 *     bucket = "bucket-$count"
 * }
 * </pre>
 * <p>
 * {@code length(subnet.arn)} returns {@code DeferredFunctionCall("length", deferred)}
 * which {@code @count} can then use to create a {@link DeferredResourceTemplate}.
 *
 * @param functionName  the function being called (e.g., "length")
 * @param deferredValue the underlying deferred value
 */
public record DeferredFunctionCall(
        String functionName,
        DeferredValue deferredValue
) {
    @Override
    public String toString() {
        return functionName + "(" + deferredValue + ")";
    }

    /**
     * Get the dependency name from the underlying deferred value.
     */
    public String dependencyName() {
        return deferredValue.dependencyName();
    }

    /**
     * Get the property path from the underlying deferred value.
     */
    public String propertyPath() {
        return deferredValue.propertyPath();
    }
}
