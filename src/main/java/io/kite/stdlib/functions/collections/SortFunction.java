package io.kite.stdlib.functions.collections;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SortFunction implements Callable {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }
        var arg = args.get(0);

        if (!(arg instanceof List<?> list)) {
            throw new RuntimeException("Argument must be an array");
        }

        var result = new ArrayList(list);
        try {
            Collections.sort(result);
        } catch (ClassCastException e) {
            throw new RuntimeException("Array elements must be comparable");
        }
        return result;
    }
}
