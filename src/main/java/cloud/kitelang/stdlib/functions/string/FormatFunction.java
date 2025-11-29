package cloud.kitelang.stdlib.functions.string;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class FormatFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.isEmpty()) {
            throw new RuntimeException("Expected at least 1 argument");
        }

        if (!(args.get(0) instanceof String format)) {
            throw new RuntimeException("First argument must be a string (format pattern)");
        }

        if (args.size() == 1) {
            return format;
        }

        try {
            Object[] formatArgs = args.subList(1, args.size()).toArray();
            return MessageFormat.format(format, formatArgs);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid format pattern: " + e.getMessage());
        }
    }
}
