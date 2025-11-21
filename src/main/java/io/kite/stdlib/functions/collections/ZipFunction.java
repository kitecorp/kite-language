package io.kite.stdlib.functions.collections;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ZipFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof List<?> list1)) {
            throw new RuntimeException("First argument must be an array");
        }
        if (!(args.get(1) instanceof List<?> list2)) {
            throw new RuntimeException("Second argument must be an array");
        }

        var result = new ArrayList<List<Object>>();
        int minSize = Math.min(list1.size(), list2.size());

        for (int i = 0; i < minSize; i++) {
            result.add(List.of(list1.get(i), list2.get(i)));
        }

        return result;
    }
}
