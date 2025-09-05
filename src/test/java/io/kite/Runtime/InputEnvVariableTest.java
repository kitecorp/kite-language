package io.kite.Runtime;

import io.kite.Runtime.Inputs.ChainResolver;
import io.kite.Runtime.Inputs.EnvResolver;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputEnvVariableTest extends InputTest {
    private Map<String, String> envVariables;

    protected void setInput(String input) {
        envVariables.put("region", input);
    }

    @Override
    protected @NotNull ChainResolver getChainResolver() {
        envVariables = new HashMap<>();
        return new ChainResolver(List.of(new EnvResolver(envVariables)));
    }

}
