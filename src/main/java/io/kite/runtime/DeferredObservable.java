package io.kite.runtime;

import io.kite.runtime.values.Deferred;
import io.kite.runtime.values.DeferredObserverValue;
import io.kite.runtime.values.ResourceValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Observer registry for managing resource dependency notifications.
 *
 * <p>This class implements the Subject role in the Observer pattern for lazy resource dependency resolution.
 * Resources with unresolved dependencies register as observers to be notified when those dependencies
 * become available.
 *
 * <h2>Example:</h2>
 * <pre>
 * resource Instance a {
 *    name = b.name  // b not yet evaluated → registers as observer
 * }
 * resource Instance b { }
 *
 * // When b completes evaluation:
 * "b" ──notifyObservers──> [a]
 * // a is re-evaluated with b.name now available
 * </pre>
 *
 * <p>See docs/DEPENDENCY_RESOLUTION.md for complete architecture documentation.
 *
 * @see DeferredObserverValue
 * @see io.kite.runtime.Interpreter#resolveDependencies
 */
public class DeferredObservable {
    private final Map<String, Set<DeferredObserverValue>> deferredResources = new HashMap<>();

    /**
     * Function called each time a resource is fully evaluated in order to notify anyone interested in this event.
     */
    public void notifyObservers(Interpreter interpreter, String resourceName) {
        // if there are observers waiting to be notified
        // the map is checked each time a resource is evaluated
        var observers = deferredResources.get(resourceName);
        if (observers == null) {
            // reached when a resource gets fully evaluated and doesn't have any observers waiting to be evaluated
            return;
        }

        for (DeferredObserverValue it : observers) {
            it.setEvaluating(true);
            it.notifyDependencyResolved(interpreter, resourceName);
        }

        observers.removeIf(DeferredObserverValue::isEvaluated);
        if (observers.isEmpty()) {
            // clean up the key as well when there are no longer resources interested in being notified by this resource
            deferredResources.remove(resourceName);
        }
    }

    public void removeObserver(DeferredObserverValue it, ResourceValue resourceValue) {
        for (String dependency : resourceValue.getDependencies()) {
            var dependencies = deferredResources.get(dependency);
            dependencies.remove(it);
        }
    }

    public void addObserver(DeferredObserverValue resource, Deferred deferred) {
        var observers = deferredResources.computeIfAbsent(deferred.resource(), k -> new HashSet<>());
        observers.add(resource);
    }
}
