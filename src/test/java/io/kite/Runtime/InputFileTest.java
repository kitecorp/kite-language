package io.kite.Runtime;

import io.kite.Runtime.Inputs.ChainResolver;
import io.kite.Runtime.Inputs.InputsDefaultsFilesFinder;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.List;

@Log4j2
public class InputFileTest extends InputTest {

    protected void setInput(String input) {
        try {
            String content = MessageFormat.format("region=\"{0}\"", input);
            var path = Path.of(InputsDefaultsFilesFinder.INPUTS_DEFAULTS_KITE);
            Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Set input file to: {}", path.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected @NotNull ChainResolver getChainResolver() {
        return new ChainResolver(List.of(new InputsDefaultsFilesFinder()));
    }

}
