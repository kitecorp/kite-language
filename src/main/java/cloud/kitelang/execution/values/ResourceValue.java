package cloud.kitelang.execution.values;

import cloud.kitelang.execution.ResourcePath;
import cloud.kitelang.execution.decorators.ProviderSupport;
import cloud.kitelang.execution.decorators.Tags;
import cloud.kitelang.execution.decorators.TagsSupport;
import cloud.kitelang.execution.environment.Environment;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResourceValue implements ProviderSupport, TagsSupport {
    /**
     * Replace with Set or List
     */
    private Environment<Object> properties;
    @Setter
    @Getter
    private SchemaValue schema;
    @Setter
    @Getter
    private String name;
    private Set<String> dependencies;
    private Set<String> providers;

    /**
     * indicate if the cloud resource
     */
    @Setter
    @Getter
    private String existing;
    @Getter
    @Setter
    private Tags tags;
    @Getter
    @Setter
    private ResourcePath path;

    /**
     * Properties that reference @cloud properties of other resources.
     * These will be resolved during apply, after the dependent resources are created.
     * Key: property name on this resource, Value: the deferred reference to resolve.
     */
    private Map<String, DeferredValue> deferredProperties;

    /**
     * Most complete factory method - all other variants chain to this one.
     */
    public static ResourceValue resourceValue(String name, Environment<Object> properties, SchemaValue schemaValue, ResourcePath path, String existing) {
        properties.setName(name);
        return ResourceValue.builder()
                .name(name)
                .schema(schemaValue)
                .properties(properties)
                .dependencies(new HashSet<>())
                .providers(new HashSet<>())
                .path(path)
                .existing(existing)
                .build();
    }

    /**
     * Factory method with ResourcePath but no existing resource.
     */
    public static ResourceValue resourceValue(String name, Environment<Object> properties, SchemaValue schemaValue, ResourcePath path) {
        return resourceValue(name, properties, schemaValue, path, null);
    }

    /**
     * Factory method with existing resource but no ResourcePath.
     */
    public static ResourceValue resourceValue(String name, Environment<Object> resourceEnv, SchemaValue installedSchema, String existing) {
        return resourceValue(name, resourceEnv, installedSchema, null, existing);
    }

    /**
     * Factory method without ResourcePath or existing resource (backwards compatibility).
     */
    public static <T> ResourceValue resourceValue(String name, Environment<Object> properties, SchemaValue schemaValue) {
        return resourceValue(name, properties, schemaValue, null, null);
    }

    public String getDatabaseKey() {
        return path != null ? path.toDatabaseKey() : getName();
    }

    public boolean isExisting() {
        return existing != null;
    }

    public Object argVal(String name) {
        return properties.get(name);
    }

    @Nullable
    public String name() {
        return name;
    }

    public Object assign(String varName, Object value) {
        return properties.assign(varName, value);
    }

    public Object lookup(@Nullable String varName) {
        return properties.lookup(varName);
    }

    public Object lookup(@Nullable String varName, Integer hops) {
        return properties.lookup(varName, hops);
    }

    public Object lookup(@Nullable Object varName) {
        return properties.lookup(varName);
    }

    public @Nullable Object get(String key) {
        return properties.get(key);
    }

    public Object init(String name, Object value) {
        return properties.init(name, value);
    }

    public void addDependency(String dependency) {
        getDependencies().add(dependency);
    }

    public Set<String> getDependencies() {
        if (dependencies == null) {
            dependencies = new HashSet<>();
        }
        return dependencies;
    }

    public boolean hasDependency(String dependency) {
        return getDependencies().contains(dependency);
    }

    public Set<String> dependenciesOrNull() {
        if (dependencies != null && dependencies.isEmpty()) {
            return null;
        }
        return dependencies;
    }

    public boolean hasDependencies() {
        return !getDependencies().isEmpty();
    }

    public void addDependency(Set<String> dependencies) {
        getDependencies().addAll(dependencies);
    }

    /**
     * Adds a deferred property reference that will be resolved during apply.
     * Also adds the dependency for topological sorting.
     *
     * @param propertyName the property on this resource that has the deferred value
     * @param deferred     the deferred reference to resolve
     */
    public void setDeferredProperty(String propertyName, DeferredValue deferred) {
        getDeferredProperties().put(propertyName, deferred);
        addDependency(deferred.dependencyName());
    }

    /**
     * Gets the map of properties with deferred cloud references.
     *
     * @return map of property name to deferred reference
     */
    public Map<String, DeferredValue> getDeferredProperties() {
        if (deferredProperties == null) {
            deferredProperties = new HashMap<>();
        }
        return deferredProperties;
    }

    /**
     * Checks if this resource has any deferred properties.
     *
     * @return true if there are deferred properties to resolve during apply
     */
    public boolean hasDeferredProperties() {
        return deferredProperties != null && !deferredProperties.isEmpty();
    }

    @Override
    public Set<String> getProviders() {
        if (providers == null) {
            this.providers = new HashSet<>();
        }
        return providers;
    }

    @Override
    public void setProviders(Set<String> providers) {
        this.providers = providers;
    }

    @Override
    public void addProvider(String provider) {
        providers.add(provider);
    }

    public Environment getProperties() {
        return properties;
    }

    public void setProperties(Environment properties) {
        this.properties = properties;
    }

    public Object getProperty(String x) {
        return getProperties().get(x);
    }
}
