package io.kite.runtime.functions.cast;

import io.kite.runtime.Callable;
import io.kite.runtime.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class StringCastFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() > 1) {
            throw new RuntimeException(MessageFormat.format("Too many arguments: {0}", args.size()));
        }
        var value = args.get(0);
        if (!(value instanceof String)) {
            return String.valueOf(value);
        }

        throw new RuntimeException("Invalid argument");
    }
}
