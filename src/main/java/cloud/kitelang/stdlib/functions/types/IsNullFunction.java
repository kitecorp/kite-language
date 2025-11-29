package cloud.kitelang.stdlib.functions.types;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.execution.values.NullValue;

import java.text.MessageFormat;
import java.util.List;

public class IsNullFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }
        return args.get(0) == null || args.get(0) instanceof NullValue;
    }
}
