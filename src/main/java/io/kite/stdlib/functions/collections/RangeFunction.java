package io.kite.stdlib.functions.collections;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class RangeFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() < 1 || args.size() > 3) {
            throw new RuntimeException(MessageFormat.format("Expected 1-3 arguments, got {0}", args.size()));
        }

        int start, end, step;

        if (args.size() == 1) {
            start = 0;
            end = ((Number) args.get(0)).intValue();
            step = 1;
        } else if (args.size() == 2) {
            start = ((Number) args.get(0)).intValue();
            end = ((Number) args.get(1)).intValue();
            step = 1;
        } else {
            start = ((Number) args.get(0)).intValue();
            end = ((Number) args.get(1)).intValue();
            step = ((Number) args.get(2)).intValue();
        }

        if (step == 0) {
            throw new RuntimeException("Step cannot be zero");
        }

        var result = new ArrayList<Integer>();
        if (step > 0) {
            for (int i = start; i < end; i += step) {
                result.add(i);
            }
        } else {
            for (int i = start; i > end; i += step) {
                result.add(i);
            }
        }
        return result;
    }
}
