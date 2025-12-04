package cloud.kitelang.stdlib.functions.utility;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.List;

public class ReadFileFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof String path)) {
            throw new RuntimeException("Argument must be a file path string");
        }

        try {
            return Files.readString(Paths.get(path));
        } catch (Exception e) {
            throw new RuntimeException("Cannot read file: " + e.getMessage());
        }
    }
}
