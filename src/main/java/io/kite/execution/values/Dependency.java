package io.kite.execution.values;

/**
 * Define a dependnecy between a resource and a value.
 */
public record Dependency(ResourceValue resource, Object value) {

}
