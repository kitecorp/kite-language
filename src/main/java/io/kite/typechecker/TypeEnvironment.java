package io.kite.typechecker;

import io.kite.environment.Environment;
import io.kite.runtime.values.ResourceValue;
import io.kite.typechecker.types.Type;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

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
}
