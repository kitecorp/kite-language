package io.kite.Runtime.Values;

import io.kite.Runtime.Environment.Environment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EqualsAndHashCode
@Builder
@AllArgsConstructor
public class ResourceValue {
    private Environment properties;
    private SchemaValue schema;
    private String name;
    private Set<String> dependencies;

    /**
     * indicate if the cloud resource
     */
    private Boolean existing;

    public ResourceValue() {
    }

    public ResourceValue(String name, Environment parent) {
        this.name = name;
        this.properties = parent;
    }

    public ResourceValue(String name, Environment parent, @NonNull SchemaValue schema) {
        this.name = name;
        this.properties = parent;
        this.schema = schema;
    }

    public ResourceValue(String name, Environment parent, @NonNull SchemaValue schema, boolean existing) {
        this(name, parent, schema);
        this.existing = existing;
    }

    public boolean isExisting() {
        return existing != null && existing;
    }

    public Boolean getExisting() {
        return existing;
    }

    public void setExisting(boolean existing) {
        this.existing = existing;
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

    public record Data(String name, Map<String, Object> args) {
    }

    public Environment getProperties() {
        return properties;
    }

    public void setProperties(Environment properties) {
        this.properties = properties;
    }

    public SchemaValue getSchema() {
        return schema;
    }

    public void setSchema(SchemaValue schema) {
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
