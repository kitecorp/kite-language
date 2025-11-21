package io.kite.stdlib.functions.numeric;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class AbsFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() > 1) {
            throw new RuntimeException(MessageFormat.format("Too many arguments: {0}", args.size()));
        }
        var arg = args.get(0);
        if (arg instanceof Double d) {
            return Math.abs(d);
        } else if (arg instanceof Float d) {
            return Math.abs(d);
        } else if (arg instanceof Number d) {
            return Math.abs(d.intValue());
        } else {
            throw new RuntimeException("Invalid argument");
        }
    }
}
