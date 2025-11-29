package cloud.kitelang.stdlib.functions.types;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class ToNumberFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }

        var arg = args.get(0);
        if (arg instanceof Number n) {
            return n;
        }
        if (arg instanceof String str) {
            try {
                if (str.contains(".")) {
                    return Double.parseDouble(str);
                } else {
                    return Integer.parseInt(str);
                }
            } catch (NumberFormatException e) {
                throw new RuntimeException("Cannot convert '" + str + "' to number");
            }
        }
        if (arg instanceof Boolean b) {
            return b ? 1 : 0;
        }
        throw new RuntimeException("Cannot convert " + arg.getClass().getSimpleName() + " to number");
    }
}
