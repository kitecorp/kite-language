package io.kite.Runtime.Values;

import io.kite.Runtime.Decorators.ProviderSupport;
import io.kite.Runtime.Decorators.Tags;
import io.kite.Runtime.Decorators.TagsSupport;
import io.kite.Runtime.Environment.Environment;
import lombok.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode
@Builder
@AllArgsConstructor
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
    private Object existing;
    private Tags tags;

    public ResourceValue() {
        this(null, null, null, false, null, null);
    }

    public ResourceValue(String name, Environment<Object> parent) {
        this(name, parent, null, false, null, null);
    }

    public ResourceValue(String name, Environment<Object> parent, @NonNull SchemaValue schema) {
        this(name, parent, schema, null, null, null);
    }

    public ResourceValue(String name, Environment<Object> parent, @NonNull SchemaValue schema, Object existing) {
        this(name, parent, schema, existing, null, null);
    }

    public ResourceValue(String name, Environment<Object> parent, @NonNull SchemaValue schema, Object existing, Set<String> dependencies, Set<String> providers) {
        this.name = name;
        this.properties = parent;
        this.schema = schema;
        this.existing = existing;
        this.dependencies = dependencies;
        this.providers = providers;
    }

    public static Object of(String string) {
        return ResourceValue.of(string, new Environment());
    }

    public static ResourceValue of(String string, Environment environment) {
        return new ResourceValue(string, environment);
    }

    public static ResourceValue of(String string, Environment environment, SchemaValue schema) {
        return new ResourceValue(string, environment, schema);
    }

    public boolean isExisting() {
        return existing != null;
    }

    public Object getExisting() {
        return existing;
    }

    public void setExisting(boolean existing) {
        this.existing = existing;
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

    @Override
    public Tags getTags() {
        if (tags == null) {
            this.tags = new Tags();
        }
        return tags;
    }

    @Override
    public void setTag(Tags tags) {
        this.tags = tags;
    }
}
