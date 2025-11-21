package io.kite.stdlib.functions.types;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;
import io.kite.execution.values.NullValue;

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
