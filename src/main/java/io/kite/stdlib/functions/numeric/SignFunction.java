package io.kite.stdlib.functions.numeric;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class SignFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }
        var arg = args.get(0);
        if (arg instanceof Number n) {
            double value = n.doubleValue();
            if (value > 0) return 1;
            if (value < 0) return -1;
            return 0;
        } else {
            throw new RuntimeException("Argument must be a number");
        }
    }
}
