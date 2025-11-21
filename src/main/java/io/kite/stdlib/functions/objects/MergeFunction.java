package io.kite.stdlib.functions.objects;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergeFunction implements Callable {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() < 2) {
            throw new RuntimeException(MessageFormat.format("Expected at least 2 arguments, got {0}", args.size()));
        }

        var result = new HashMap();

        for (Object arg : args) {
            if (!(arg instanceof Map map)) {
                throw new RuntimeException("All arguments must be objects");
            }
            result.putAll(map);
        }

        return result;
    }
}
