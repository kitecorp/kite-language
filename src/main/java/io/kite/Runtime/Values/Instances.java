package io.kite.Runtime.Values;

import io.kite.Runtime.exceptions.DeclarationExistsException;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;


public class Instances {
    private final Map<String, ResourceValue> instances = new HashMap<>();

    private Instances() {
    }

    public ResourceValue initInstance(ResourceValue instance) {
        var contains = this.instances.containsKey(instance.name()); // todo performance tip: replace contains with put only
        if (contains) {
            throw new DeclarationExistsException(">" + instance.name() + "< already exists in schema");
        }
        return this.instances.put(instance.name(), instance);
    }

    @Nullable
    public ResourceValue getInstance(String name) {
        return instances.get(name);
    }
}
