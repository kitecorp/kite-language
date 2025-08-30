package io.kite.Runtime.Inputs;

import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Runtime.Environment.Environment;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class EnvResolver extends InputResolver {

    public EnvResolver(Environment<Object> inputs) {
        super(inputs);
    }

    @Override
    public @Nullable Object resolve(InputDeclaration key) {
        var value = System.getenv("KITE_INPUT_" + StringUtils.upperCase(key.name()));
        if (value == null) {
            return null;
        }
        getInputs().init(key.name(), value);
        return value;
    }
}
