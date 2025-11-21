package io.kite.stdlib.functions.string;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class SubstringFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() < 2 || args.size() > 3) {
            throw new RuntimeException(MessageFormat.format("Expected 2 or 3 arguments, got {0}", args.size()));
        }
        var str = args.get(0);
        if (!(str instanceof String s)) {
            throw new RuntimeException("First argument must be a string");
        }
        var startIdx = args.get(1);
        if (!(startIdx instanceof Number start)) {
            throw new RuntimeException("Second argument must be a number");
        }

        if (args.size() == 2) {
            return s.substring(start.intValue());
        } else {
            var endIdx = args.get(2);
            if (!(endIdx instanceof Number end)) {
                throw new RuntimeException("Third argument must be a number");
            }
            return s.substring(start.intValue(), end.intValue());
        }
    }
}
