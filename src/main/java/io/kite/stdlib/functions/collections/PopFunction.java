package io.kite.stdlib.functions.collections;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class PopFunction implements Callable {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }
        var arg = args.get(0);

        if (!(arg instanceof List list)) {
            throw new RuntimeException("Argument must be an array");
        }

        if (list.isEmpty()) {
            throw new RuntimeException("Array is empty");
        }

        var result = new ArrayList(list);
        result.remove(result.size() - 1);
        return result;
    }
}
