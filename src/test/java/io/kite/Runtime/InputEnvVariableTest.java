package io.kite.Runtime;

import io.kite.Runtime.Inputs.EnvResolver;
import io.kite.Runtime.Inputs.InputResolver;
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
    protected @NotNull List<InputResolver> getResolvers() {
        envVariables = new HashMap<>();
        return List.of(new EnvResolver(envVariables));
    }

}
