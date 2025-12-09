package cloud.kitelang.execution.values;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a reference to a resource that may or may not be resolved yet.
 * Unified type replacing both Deferred and Dependency.
 *
 * <p>This sealed interface provides type safety and better debugging context
 * for resource dependency tracking during interpretation.
 *
 * <h2>Usage:</h2>
 * <pre>
 * // When resource doesn't exist yet
 * ResourceRef ref = ResourceRef.pending("other");
 *
 * // When resource is resolved
 * ResourceRef ref = ResourceRef.resolved(resourceValue, propertyValue);
 *
 * // Pattern matching
 * switch (ref) {
 *     case ResourceRef.Pending p -> // handle unresolved
 *     case ResourceRef.Resolved r -> // handle resolved
 * }
 * </pre>
 *
 * @see DeferredObservable
 * @see cloud.kitelang.execution.Interpreter#resolveDependencies
 */
public sealed interface ResourceRef permits ResourceRef.Pending, ResourceRef.Resolved {

    /**
     * The resource name being referenced.
     */
    String resourceName();

    /**
     * Whether this reference is resolved.
     */
    default boolean isResolved() {
        return this instanceof Resolved;
    }

    /**
     * Pending reference - resource not yet evaluated.
     * Replaces the old Deferred record.
     *
     * @param resourceName  the name of the referenced resource
     * @param propertyPath  the property being accessed (e.g., "name" for other.name), null for full resource
     * @param source        where this reference was created, for debugging
     */
    record Pending(
            String resourceName,
            @Nullable String propertyPath,
            RefSource source
    ) implements ResourceRef {}

    /**
     * Resolved reference - resource evaluated, value available.
     * Replaces the old Dependency record.
     *
     * @param resourceName the name of the referenced resource
     * @param resource     the resolved ResourceValue (null if not a resource type)
     * @param value        the actual value being accessed
     */
    record Resolved(
            String resourceName,
            @Nullable ResourceValue resource,
            Object value
    ) implements ResourceRef {}

    /**
     * Tracks the origin of the reference for debugging purposes.
     */
    enum RefSource {
        /** Property access like other.name */
        PROPERTY_ACCESS,
        /** @dependsOn decorator */
        DEPENDS_ON,
        /** [for i in resources] loop */
        FOR_LOOP_INDEX,
        /** Array/index access like resources[0] */
        ARRAY_ACCESS
    }

    // ============ Factory Methods ============

    /**
     * Creates a pending reference for a resource that hasn't been evaluated yet.
     *
     * @param name the resource name
     * @return a Pending reference with default source PROPERTY_ACCESS
     */
    static Pending pending(String name) {
        return new Pending(name, null, RefSource.PROPERTY_ACCESS);
    }

    /**
     * Creates a pending reference with full context.
     *
     * @param name     the resource name
     * @param property the property path being accessed (null for full resource)
     * @param source   where this reference originated
     * @return a Pending reference with full context
     */
    static Pending pending(String name, @Nullable String property, RefSource source) {
        return new Pending(name, property, source);
    }

    /**
     * Creates a resolved reference wrapping a resource and its accessed value.
     *
     * @param resource the resolved ResourceValue
     * @param value    the property value being accessed
     * @return a Resolved reference
     */
    static Resolved resolved(ResourceValue resource, Object value) {
        return new Resolved(resource.getName(), resource, value);
    }

    /**
     * Creates a resolved reference for a non-resource value.
     *
     * @param name  the identifier name
     * @param value the resolved value
     * @return a Resolved reference without a ResourceValue
     */
    static Resolved resolvedValue(String name, Object value) {
        return new Resolved(name, null, value);
    }
}
