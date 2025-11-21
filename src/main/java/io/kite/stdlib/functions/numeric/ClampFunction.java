package io.kite.stdlib.functions.numeric;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class ClampFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 3) {
            throw new RuntimeException(MessageFormat.format("Expected 3 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof Number value)) {
            throw new RuntimeException("First argument must be a number");
        }
        if (!(args.get(1) instanceof Number min)) {
            throw new RuntimeException("Second argument must be a number");
        }
        if (!(args.get(2) instanceof Number max)) {
            throw new RuntimeException("Third argument must be a number");
        }

        double v = value.doubleValue();
        double minVal = min.doubleValue();
        double maxVal = max.doubleValue();

        if (v < minVal) return minVal;
        if (v > maxVal) return maxVal;
        return v;
    }
}
