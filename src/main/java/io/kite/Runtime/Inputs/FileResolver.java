package io.kite.Runtime.Inputs;

import io.kite.Frontend.Parser.Expressions.InputDeclaration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class FileResolver extends InputResolver {
    private Map<String, Object> inputs;

    public FileResolver(File... files) {
        inputs = new HashMap<>();
        for (File file : files) {
            try (Stream<String> stream = Files.lines(file.toPath())) {
                stream.forEach(line -> {
                    var input = line.split("=");
                    inputs.put(input[0], input[1]);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String resolve(InputDeclaration input) {
//        return getInputs().get(input.name());
        return null;
    }

}
