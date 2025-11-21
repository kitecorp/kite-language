package io.kite.stdlib.functions.collections;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

public class JoinFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() < 1 || args.size() > 2) {
            throw new RuntimeException(MessageFormat.format("Expected 1 or 2 arguments, got {0}", args.size()));
        }
        var arg = args.get(0);
        if (!(arg instanceof List<?> list)) {
            throw new RuntimeException("First argument must be an array");
        }

        var delimiter = args.size() == 2 ? args.get(1).toString() : "";
        return list.stream()
                .map(Object::toString)
                .collect(Collectors.joining(delimiter));
    }
}
