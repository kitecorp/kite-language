package io.kite.Runtime.Inputs;

import io.kite.Runtime.Environment.Environment;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChainResolver extends InputResolver {
    private List<InputResolver> resolvers;

    public ChainResolver(Environment<Object> environment, List<InputResolver> resolvers) {
        super(environment);
        this.resolvers = resolvers;
    }

    @Override
    public @Nullable Object resolve(String key) {
        Object value = null;
        for (InputResolver resolver : resolvers) {
            value = resolver.resolve(key);
        }
        return value;
    }
}
