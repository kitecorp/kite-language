package io.zmeu.TypeChecker;

import io.zmeu.Runtime.Environment.Environment;
import io.zmeu.Runtime.Values.ResourceValue;
import io.zmeu.TypeChecker.Types.Type;
import lombok.extern.log4j.Log4j2;
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

    public TypeEnvironment(@Nullable Environment<Type> parent, Map<String, Type> variables) {
        super(parent, variables);
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
}
