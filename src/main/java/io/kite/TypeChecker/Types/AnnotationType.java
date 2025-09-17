package io.kite.TypeChecker.Types;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public final class AnnotationType extends Type {
    private List<Type> params;
    private Set<Target> targets;

    public AnnotationType(String name, List<Type> params, Set<Target> targets) {
        super(SystemType.ANNOTATION);
        setValue(name);
        this.params = List.copyOf(params);
        this.targets = targets;
    }

    public static Object annotation(String name, List<Type> params, Set<Target> targets) {
        return new AnnotationType(name, params, targets);
    }

    public static Object annotation(String name, Target... targets) {
        return new AnnotationType(name, List.of(), Set.of(targets));
    }

    public enum Target {
        OUTPUT, INPUT, RESOURCE, SCHEMA, SCHEMA_PROPERTY;
    }

}
