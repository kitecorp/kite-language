package io.kite.Runtime;

import io.kite.Runtime.Inputs.ChainResolver;
import io.kite.Runtime.Inputs.InputsDefaultsFilesFinder;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@Log4j2
public class InputFileTests extends InputTests {

    protected void setInput(String input) {
        InputsDefaultsFilesFinder.writeToDefaults(Map.of("region", input));
    }

    @Override
    protected void setInput(Integer input) {
        InputsDefaultsFilesFinder.writeToDefaults(Map.of("region", input));
    }

    @Override
    protected void setInput(Boolean input) {
        InputsDefaultsFilesFinder.writeToDefaults(Map.of("region", input));
    }

    @Override
    protected void setInput(Double input) {
        InputsDefaultsFilesFinder.writeToDefaults(Map.of("region", input));
    }

    @Override
    protected void setInput(Float input) {
        InputsDefaultsFilesFinder.writeToDefaults(Map.of("region", input));
    }

    @Override
    protected @NotNull ChainResolver getChainResolver() {
        return new ChainResolver(List.of(new InputsDefaultsFilesFinder()));
    }

}
