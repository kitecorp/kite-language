package io.kite.stdlib.functions.collections;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;
import io.kite.execution.values.NullValue;

import java.text.MessageFormat;
import java.util.List;

public class FindFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof List<?> list)) {
            throw new RuntimeException("First argument must be an array");
        }

        var searchValue = args.get(1);

        for (Object item : list) {
            if (item != null && item.equals(searchValue)) {
                return item;
            }
        }

        return new NullValue();
    }
}
