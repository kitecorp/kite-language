package cloud.kitelang.semantics;

import cloud.kitelang.execution.environment.Environment;
import cloud.kitelang.execution.values.ResourceValue;
import cloud.kitelang.semantics.types.ResourceType;
import cloud.kitelang.semantics.types.Type;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * Mapping from names to types
 * In type theory is called Gamma
 *
 * */
@Log4j2
public class TypeEnvironment extends Environment<Type> {
    public TypeEnvironment(@Nullable Environment<Type> parent) {
        super(parent);
    }

    public TypeEnvironment(String name, @Nullable Environment<Type> parent) {
        super(name, parent);
    }

    public TypeEnvironment(String name) {
        super(name);
    }

    public TypeEnvironment(@Nullable Environment<Type> parent, Map<String, Type> variables) {
        super(parent, variables);
    }
    public TypeEnvironment(String name,@Nullable Environment<Type> parent, Map<String, Type> variables) {
        super(parent, variables);
        setName(name);
    }

    public TypeEnvironment(@Nullable Environment<Type> parent, ResourceValue variables) {
        super(parent, variables);
    }

    public TypeEnvironment(Map<String, Type> variables) {
        super(variables);
    }

    public TypeEnvironment() {
        super();
    }

    public boolean hasName() {
        return StringUtils.isNotBlank(getName());
    }

    /**
     * Returns the root (top-most) TypeEnvironment in the hierarchy.
     * Resources are stored at the root level to ensure global uniqueness.
     */
    public TypeEnvironment getTypeRoot() {
        var parent = getParent();
        if (parent == null) {
            return this;
        }
        if (parent instanceof TypeEnvironment typeParent) {
            return typeParent.getTypeRoot();
        }
        return this;
    }

    /**
     * Returns all ResourceType instances stored in this environment.
     * Preserves insertion order using LinkedHashMap for deterministic dependency resolution.
     */
    public Map<String, ResourceType> getResourceTypes() {
        return getVariables().entrySet().stream()
                .filter(e -> e.getValue() instanceof ResourceType)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (ResourceType) e.getValue(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /**
     * Checks if a resource type with the given name exists anywhere in the environment hierarchy.
     * Used to ensure global uniqueness of resource names.
     */
    public boolean hasResourceTypeGlobally(String name) {
        var value = get(name);
        if (value instanceof ResourceType) {
            return true;
        }
        var parent = getParent();
        if (parent instanceof TypeEnvironment typeParent) {
            return typeParent.hasResourceTypeGlobally(name);
        }
        return false;
    }

    /**
     * Initializes a resource type with global uniqueness checking.
     * Checks for duplicates across the entire hierarchy but stores in the CURRENT scope.
     * Use this for component resources that need to be in the component's environment.
     *
     * @param name     the resource name (segment name including index if applicable)
     * @param resource the ResourceType to register
     * @return the registered resource type
     */
    public ResourceType initResourceType(String name, ResourceType resource) {
        if (hasResourceTypeGlobally(name)) {
            throw new TypeError("Resource '" + name + "' already exists");
        }
        // Store in CURRENT scope - important for component scoping
        getVariables().put(name, resource);
        return resource;
    }

    /**
     * Initializes a resource type at the ROOT environment level.
     * Use this for top-level resources to catch conflicts across sibling scopes (e.g., two for-loops).
     *
     * @param name     the resource name (segment name including index if applicable)
     * @param resource the ResourceType to register
     * @return the registered resource type
     */
    public ResourceType initResourceTypeAtRoot(String name, ResourceType resource) {
        var root = getTypeRoot();
        if (root.hasResourceTypeGlobally(name)) {
            throw new TypeError("Resource '" + name + "' already exists");
        }
        root.getVariables().put(name, resource);
        return resource;
    }

    /**
     * Retrieves a resource type by name from the root environment.
     */
    @Nullable
    public ResourceType getResourceType(String name) {
        var value = getTypeRoot().get(name);
        return value instanceof ResourceType rt ? rt : null;
    }
}
