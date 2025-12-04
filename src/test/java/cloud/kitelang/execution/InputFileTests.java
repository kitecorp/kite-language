package cloud.kitelang.execution;

import cloud.kitelang.execution.inputs.InputChainResolver;
import cloud.kitelang.execution.inputs.InputsFilesResolver;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;

import java.util.List;
import java.util.Map;

@Log4j2
public class InputFileTests extends InputTests {

    @AfterEach
    void cleanup() {
        InputsFilesResolver.deleteDefaults();
    }

    protected void setInput(String input) {
        InputsFilesResolver.writeToDefaults(Map.of("region", input));
    }

    @Override
    protected void setInput(Integer input) {
        InputsFilesResolver.writeToDefaults(Map.of("region", input));
    }

    @Override
    protected void setInput(Boolean input) {
        InputsFilesResolver.writeToDefaults(Map.of("region", input));
    }

    @Override
    protected void setInput(Double input) {
        InputsFilesResolver.writeToDefaults(Map.of("region", input));
    }

    @Override
    protected void setInput(Float input) {
        InputsFilesResolver.writeToDefaults(Map.of("region", input));
    }

    @Override
    protected @NotNull InputChainResolver getChainResolver() {
        return new InputChainResolver(List.of(new InputsFilesResolver()));
    }

}
