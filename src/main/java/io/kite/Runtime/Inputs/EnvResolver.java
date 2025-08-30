package io.kite.Runtime.Inputs;

import io.kite.Runtime.Environment.Environment;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

public class EnvResolver extends InputResolver {

    public EnvResolver(Environment<Object> inputs) {
        super(inputs);
    }

    @Override
    public @Nullable Object resolve(String key) {
        var value = System.getenv("KITE_INPUT_" + StringUtils.upperCase(key));
        if (value == null) {
            return null;
        }
        getInputs().init(key, value);
        return value;
    }
}
