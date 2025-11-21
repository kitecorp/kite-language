package io.kite.stdlib.functions.utility;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;
import io.kite.execution.values.NullValue;

import java.text.MessageFormat;
import java.util.List;

public class EnvFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() < 1 || args.size() > 2) {
            throw new RuntimeException(MessageFormat.format("Expected 1 or 2 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof String varName)) {
            throw new RuntimeException("First argument must be a string (environment variable name)");
        }

        String value = System.getenv(varName);
        if (value != null) {
            return value;
        }

        return args.size() == 2 ? args.get(1) : new NullValue();
    }
}
