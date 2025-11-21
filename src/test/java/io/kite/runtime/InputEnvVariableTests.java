package io.kite.runtime;

import io.kite.runtime.inputs.EnvResolver;
import io.kite.runtime.inputs.InputChainResolver;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InputEnvVariableTests extends InputTests {
    private Map<String, Object> envVariables;

    protected void setInput(String input) {
        envVariables.put("region", input);
    }

    @Override
    protected void setInput(Integer input) {
        envVariables.put("region", input);
    }

    @Override
    protected void setInput(Boolean input) {
        envVariables.put("region", input);
    }

    @Override
    protected void setInput(Double input) {
        envVariables.put("region", input);
    }

    @Override
    protected void setInput(Float input) {
        envVariables.put("region", input);
    }

    @Override
    protected @NotNull InputChainResolver getChainResolver() {
        envVariables = new HashMap<>();
        return new InputChainResolver(List.of(new EnvResolver(envVariables)));
    }

}
