package io.kite.Runtime.Inputs;

import io.kite.Frontend.Parser.Expressions.InputDeclaration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FileResolver extends InputResolver {
    public static final String INPUTS_DEFAULTS_KITE = "inputs.defaults.kite";
    static final String INPUTS_ENV_DEFAULTS_KITE = "inputs.%s.defaults.kite";
    private Map<String, String> inputs;
    private boolean wasRead = false;

    public FileResolver() {
        inputs = new HashMap<>();
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
            throw new RuntimeException(e);
        }
    }

    @Override
    public String resolve(InputDeclaration input) {
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
