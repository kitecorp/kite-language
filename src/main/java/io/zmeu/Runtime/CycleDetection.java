package io.zmeu.Runtime;

import io.zmeu.Runtime.Values.ResourceValue;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.Set;

@Log4j2
public class CycleDetection {
    /**
     * given 2 resources:
     * resource Type x {
     * name = Type.y.name
     * }
     * resource Type y {
     * name = Type.x.name
     * }
     * when y.Type.x.name returns a deferred(y) it means it points to itself
     * because the deferred comes from x which waits for y to be evaluated.
     * This works for:
     * 1. direct cycles: a -> b and b -> a
     * 2. indirect cycles: a->b->c->a
     */
    public static void detect(ResourceValue resource) {
        Set<String> visited = new HashSet<>();
        Set<String> activePath = new HashSet<>();

        detectCycles(resource, visited, activePath);
    }

    private static void detectCycles(ResourceValue resource, Set<String> visited, Set<String> activePath) {
        // If the resource is already in the active path, a cycle exists
        if (activePath.contains(resource.name())) {
            String message = "Cycle detected at resource: " + resource.name();
            log.error(message);
            throw new RuntimeException(message);
        }

        // If the resource is already visited, skip it
        if (visited.contains(resource.name())) {
            return;
        }

        // Mark resource as visited and add it to the active path
        visited.add(resource.name());
        activePath.add(resource.name());

        // Recurse into all dependencies
        for (String dependencyName : resource.getDependencies()) {
            var dependency = resource.getSchema().getInstance(dependencyName); // Implement this lookup
            if (dependency != null) {
                detectCycles(dependency, visited, activePath);
            }
        }

        // Remove the resource from the active path after processing
        activePath.remove(resource.name());
    }

}
