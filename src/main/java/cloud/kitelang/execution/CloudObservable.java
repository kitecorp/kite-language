package cloud.kitelang.execution;

import cloud.kitelang.execution.values.CloudPropertyObserver;
import cloud.kitelang.execution.values.ResourceValue;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * Observer registry for cloud property dependencies.
 * Similar to DeferredObservable but for values resolved during apply.
 *
 * <p>When a decorator (like @count) depends on a @cloud property, a CloudPropertyObserver
 * is registered here. After apply creates the dependency and cloud values become available,
 * notifyObservers() re-evaluates the deferred decorators.
 *
 * <h2>Example:</h2>
 * <pre>
 * @count(vpc.subnetCount)  // subnetCount is @cloud number
 * resource Subnet subnet {
 *     cidrBlock = "10.0.$count.0/24"
 * }
 *
 * // During plan: vpc.subnetCount returns DeferredValue
 * // → CloudPropertyObserver registered for "vpc"
 * // → subnet marked as cloud-pending
 *
 * // During apply: vpc created → subnetCount = 3
 * // → notifyObservers("vpc", cloudValues)
 * // → @count re-evaluated → now returns 3
 * // → subnet[0], subnet[1], subnet[2] created
 * </pre>
 *
 * @see CloudPropertyObserver
 * @see DeferredObservable
 */
@Slf4j
public class CloudObservable {
    private final Map<String, Set<CloudPropertyObserver>> observers = new HashMap<>();

    /**
     * Register an observer waiting for a cloud property to be resolved.
     *
     * @param resourceName the resource whose cloud property is needed
     * @param observer     the observer to notify when cloud values are available
     */
    public void addObserver(String resourceName, CloudPropertyObserver observer) {
        observers.computeIfAbsent(resourceName, k -> new HashSet<>()).add(observer);
        log.debug("Registered cloud observer for '{}': {}", resourceName, observer.getTemplateName());
    }

    /**
     * Called after a resource is created in cloud with resolved properties.
     * Re-evaluates all observers waiting on this resource.
     *
     * @param interpreter   the interpreter to use for re-evaluation
     * @param resourceName  the resource that was created
     * @param cloudValues   the cloud-assigned property values
     * @return list of ResourceValues created by re-evaluation
     */
    public List<ResourceValue> notifyObservers(
            Interpreter interpreter,
            String resourceName,
            Map<String, Object> cloudValues) {

        var waiting = observers.remove(resourceName);
        if (waiting == null || waiting.isEmpty()) {
            return List.of();
        }

        log.info("Notifying {} observer(s) waiting on '{}'", waiting.size(), resourceName);

        // Inject cloud values into environment so expressions resolve
        injectCloudValues(interpreter, resourceName, cloudValues);

        // Re-evaluate each observer
        var results = new ArrayList<ResourceValue>();
        for (var observer : waiting) {
            try {
                var created = observer.reEvaluate(interpreter);
                if (created != null) {
                    results.addAll(created);
                    log.debug("Observer '{}' created {} resource(s)",
                            observer.getTemplateName(), created.size());
                }
            } catch (Exception e) {
                log.error("Failed to re-evaluate observer '{}': {}",
                        observer.getTemplateName(), e.getMessage());
                throw e;
            }
        }

        return results;
    }

    /**
     * Check if there are any observers waiting for cloud values.
     *
     * @return true if there are pending observers
     */
    public boolean hasObservers() {
        return !observers.isEmpty();
    }

    /**
     * Get all resource names that have observers waiting.
     *
     * @return set of resource names
     */
    public Set<String> getPendingResources() {
        return new HashSet<>(observers.keySet());
    }

    /**
     * Inject cloud values into the interpreter environment.
     * This allows expressions like vpc.vpcId to resolve to actual cloud values.
     */
    private void injectCloudValues(Interpreter interpreter, String resourceName, Map<String, Object> cloudValues) {
        var env = interpreter.getEnv();

        // Look up the existing resource in the environment
        var existing = env.lookup(resourceName);
        if (existing instanceof ResourceValue resourceValue) {
            // Update the resource's properties with cloud values
            // Use getVariables() to access the underlying map since put() is private
            var propsMap = resourceValue.getProperties().getVariables();
            for (var entry : cloudValues.entrySet()) {
                propsMap.put(entry.getKey(), entry.getValue());
            }
            log.debug("Injected {} cloud values into '{}'", cloudValues.size(), resourceName);
        } else {
            log.warn("Could not find ResourceValue '{}' to inject cloud values", resourceName);
        }
    }
}
