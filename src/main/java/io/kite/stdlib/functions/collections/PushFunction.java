package io.kite.stdlib.functions.collections;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class PushFunction implements Callable {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }
        var collection = args.get(0);
        var element = args.get(1);

        if (!(collection instanceof List list)) {
            throw new RuntimeException("First argument must be an array");
        }

        var result = new ArrayList(list);
        result.add(element);
        return result;
    }
}
