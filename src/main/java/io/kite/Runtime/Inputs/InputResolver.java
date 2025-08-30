package io.kite.Runtime.Inputs;

import io.kite.Runtime.Environment.Environment;
import org.jetbrains.annotations.Nullable;

public abstract class InputResolver {
    private final Environment<Object> inputs;

    public InputResolver(Environment<Object> inputs) {
        this.inputs = inputs;
    }

    @Nullable
    public abstract Object resolve(String key);

    public Environment<Object> getInputs() {
        return inputs;
    }
}
