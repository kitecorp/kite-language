package cloud.kitelang.execution;

import cloud.kitelang.execution.values.ResourceRef;
import cloud.kitelang.execution.values.ResourceValue;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Support interface for resource lookup with deferred/pending reference handling.
 * Used during dependency resolution to handle forward references.
 */
public interface CycleDetectionSupport {

    /**
     * Looks up a resource by name, returning a ResourceRef that's either Pending or Resolved.
     * If the resource doesn't exist yet, returns a Pending reference.
     * If the resource exists, returns a Resolved reference.
     *
     * @param environment the environment to look up in
     * @param name        the resource name to look up
     * @param <T>         the value type in the environment
     * @return ResourceRef.Pending if not found, ResourceRef.Resolved if found
     */
    static <T> ResourceRef lookupOrPending(Map<String, T> environment, String name) {
        T value = environment.get(name);
        if (value == null) {
            return ResourceRef.pending(name);
        }
        if (value instanceof ResourceValue rv) {
            return ResourceRef.resolved(rv, rv);
        }
        // For non-resource values, wrap in resolved
        return ResourceRef.resolvedValue(name, value);
    }

    /**
     * Looks up a resource by name, returning a ResourceRef with source context.
     *
     * @param environment the environment to look up in
     * @param name        the resource name to look up
     * @param source      the origin of this lookup (for debugging)
     * @param <T>         the value type in the environment
     * @return ResourceRef.Pending if not found, ResourceRef.Resolved if found
     */
    static <T> ResourceRef lookupOrPending(Map<String, T> environment, String name, ResourceRef.RefSource source) {
        T value = environment.get(name);
        if (value == null) {
            return ResourceRef.pending(name, null, source);
        }
        if (value instanceof ResourceValue rv) {
            return ResourceRef.resolved(rv, rv);
        }
        return ResourceRef.resolvedValue(name, value);
    }

    /**
     * @deprecated Use {@link #lookupOrPending(Map, String)} instead.
     * Returns Object for backward compatibility during migration.
     */
    @Deprecated(forRemoval = true)
    static @Nullable <T> Object propertyOrDeferred(Map<String, T> environment, String name) {
        T t = environment.get(name);
        // if instance was not installed yet -> it will be installed later so we return a pending reference
        if (t == null) {
            return ResourceRef.pending(name);
        }
        return t;
    }
}
