package io.kite.stdlib.functions.numeric;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class ModFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof Number dividend)) {
            throw new RuntimeException("First argument must be a number");
        }
        if (!(args.get(1) instanceof Number divisor)) {
            throw new RuntimeException("Second argument must be a number");
        }

        return dividend.doubleValue() % divisor.doubleValue();
    }
}
