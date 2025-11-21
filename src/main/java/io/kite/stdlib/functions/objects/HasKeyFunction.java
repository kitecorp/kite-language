package io.kite.stdlib.functions.objects;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

public class HasKeyFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof Map<?, ?> map)) {
            throw new RuntimeException("First argument must be an object");
        }

        return map.containsKey(args.get(1));
    }
}
