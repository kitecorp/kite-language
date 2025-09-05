package io.kite.Runtime.Inputs;

import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Log4j2
public class InputsDefaultsFilesFinder extends InputResolver {
    static final String INPUTS_DEFAULTS_KITE = "inputs.default.kite";
    static final String INPUTS_ENV_DEFAULTS_KITE = "inputs.%s.default.kite";
    private Map<String, String> inputs;
    private boolean wasRead = false;

    public InputsDefaultsFilesFinder() {
        inputs = new HashMap<>();
    }

    /**
     * Used for testing.
     */
    public static void writeToDefaults(Map<String, String> values) {
        try {
            var content = values.entrySet().stream().map(it -> it.getKey() + "=\"" + it.getValue() + "\"").reduce((a, b) -> a + "\n" + b);

            var path = Path.of(InputsDefaultsFilesFinder.INPUTS_DEFAULTS_KITE);
            Files.writeString(path, content.get(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Set input file to: {}", path.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readFileProperty(Path file) {
        try (var stream = Files.lines(file)) {
            stream.forEach(line -> {
                var input = line.split("=");
                if (input.length == 2) {
                    inputs.put(input[0], input[1].replaceAll("^['\"]|['\"]$", ""));
                }
            });
        } catch (IOException e) {
            log.info("File not present file: {}", file.toAbsolutePath());
        }
    }

    @Override
    String resolve(InputDeclaration input, String previousValue) {
        // read default file
        if (!wasRead) {
            readFileProperty(Paths.get(INPUTS_DEFAULTS_KITE));

            // read environment specific file. env can be specified using KITE_ENV variable.
            Optional.ofNullable(System.getenv(EnvVariablesConstants.KITE_ENV))
                    .ifPresent(env -> readFileProperty(Paths.get(INPUTS_ENV_DEFAULTS_KITE.formatted(env))));
            wasRead = true;
        }
        return inputs.get(input.name());
    }

}
