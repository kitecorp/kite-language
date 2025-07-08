package io.zmeu.TypeChecker;

import io.zmeu.Runtime.Environment.Environment;
import io.zmeu.Runtime.Values.ResourceValue;
import io.zmeu.TypeChecker.Types.ReferenceType;
import io.zmeu.TypeChecker.Types.Type;
import io.zmeu.TypeChecker.Types.TypeFactory;
import io.zmeu.TypeChecker.Types.ValueType;
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
        for (var value : ValueType.values()) {
            init(value.getValue(), value);
        }
        for (var value : ReferenceType.values()) {
            init(value.getValue(), value);
        }
        init("pow", TypeFactory.fromString("(%s,%s)->%s".formatted(ValueType.Number.getValue(), ValueType.Number.getValue(), ValueType.Number.getValue())));
        init("toString", TypeFactory.fromString("(%s)->%s".formatted(ValueType.Number.getValue(), ValueType.String.getValue())));
    }
}
