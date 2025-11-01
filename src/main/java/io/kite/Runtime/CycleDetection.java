package io.kite.Runtime;

import io.kite.Runtime.Values.ResourceValue;
import io.kite.Runtime.exceptions.NotFoundException;
import lombok.extern.log4j.Log4j2;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
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
    public static void detect(ResourceValue resource, Interpreter interpreter) {
        Set<String> visited = new HashSet<>();
        Set<String> activePath = new HashSet<>();

        detectCycles(resource, visited, activePath, interpreter);
    }

    private static void detectCycles(ResourceValue resource, Set<String> visited, Set<String> activePath, Interpreter interpreter) {
        // If the resource is already in the active path, a cycle exists
        if (activePath.contains(resource.name())) {
            String message = "Cycle detected at resource: " + resource.name();
            log.error(message);
            throw new CycleException(message);
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
            var dependency = interpreter.getInstance(dependencyName); // Implement this lookup
            if (dependency != null) {
                detectCycles(dependency, visited, activePath, interpreter);
            }
        }

        // Remove the resource from the active path after processing
        activePath.remove(resource.name());
    }


    /**
     * Sorts the resources topologically by their dependencies.
     */
    static <R extends ResourceValue> LinkedHashMap<String, R> topologySort(Map<String, R> resources) {
        var out = new LinkedHashMap<String, R>(resources.size());
        Set<String> seen = new HashSet<>();
        for (var entry : resources.entrySet()) {
            dfs(entry.getKey(), resources, seen, out);
        }
        return out;
    }

    private static <R extends ResourceValue> void dfs(String name, Map<String, R> resources,
                                                      Set<String> seen, Map<String, R> out) {
        if (!seen.add(name)) return;
        R res = resources.get(name);
        if (res == null) {
            throw new NotFoundException("resource not defined: " + name);
        }
        for (String dep : res.getDependencies()) {
            dfs(dep, resources, seen, out);
        }
        out.put(name, res); // insertion order now reflects dependency order
    }

}
