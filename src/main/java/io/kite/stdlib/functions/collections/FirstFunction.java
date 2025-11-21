package io.kite.stdlib.functions.collections;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class FirstFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }
        var arg = args.get(0);
        if (arg instanceof String str) {
            if (str.isEmpty()) {
                throw new RuntimeException("String is empty");
            }
            return String.valueOf(str.charAt(0));
        } else if (arg instanceof List<?> list) {
            if (list.isEmpty()) {
                throw new RuntimeException("Array is empty");
            }
            return list.get(0);
        } else {
            throw new RuntimeException("Argument must be a string or array");
        }
    }
}
