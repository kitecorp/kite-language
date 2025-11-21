package io.kite.stdlib.functions.string;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class CharAtFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof String str)) {
            throw new RuntimeException("First argument must be a string");
        }
        if (!(args.get(1) instanceof Number index)) {
            throw new RuntimeException("Second argument must be a number");
        }

        int idx = index.intValue();
        if (idx < 0 || idx >= str.length()) {
            throw new RuntimeException(MessageFormat.format("Index {0} out of bounds for string of length {1}", idx, str.length()));
        }

        return String.valueOf(str.charAt(idx));
    }
}
