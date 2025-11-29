package cloud.kitelang.stdlib.functions.utility;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

public class FileExistsFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof String path)) {
            throw new RuntimeException("Argument must be a file path string");
        }

        return new File(path).exists();
    }
}
