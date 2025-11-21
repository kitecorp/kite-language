package io.kite.stdlib.functions.collections;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class TakeFunction implements Callable {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof List list)) {
            throw new RuntimeException("First argument must be an array");
        }
        if (!(args.get(1) instanceof Number n)) {
            throw new RuntimeException("Second argument must be a number");
        }

        int count = n.intValue();
        return list.subList(0, Math.min(count, list.size()));
    }
}
