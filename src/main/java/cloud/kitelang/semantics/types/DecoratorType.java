package cloud.kitelang.semantics.types;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@EqualsAndHashCode(callSuper = true)
public final class DecoratorType extends Type {
    private List<Type> params;
    private Set<Target> targets;

    public DecoratorType(List<Type> params, Set<Target> targets) {
        super(SystemType.DECORATOR);
        this.params = List.copyOf(params);
        this.targets = targets;
    }

    public static DecoratorType decorator(List<Type> params, Set<Target> targets) {
        return new DecoratorType(params, targets);
    }

    public static DecoratorType decorator(List<Type> params, Target targets) {
        return new DecoratorType(params, Set.of(targets));
    }

    public static DecoratorType decorator(Target... targets) {
        return new DecoratorType(List.of(), Set.of(targets));
    }

    public String targetString() {
        return targets.stream().map(Target::lowercase).collect(Collectors.joining(", "));
    }

    public enum Target {
        OUTPUT, INPUT, VAR, RESOURCE, COMPONENT, SCHEMA, SCHEMA_PROPERTY, STRUCT, STRUCT_PROPERTY, FUN;

        String lowercase() {
            return toString().toLowerCase();
        }
    }

}
