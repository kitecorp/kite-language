package io.kite.stdlib.functions.objects;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;
import io.kite.execution.values.NullValue;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

public class GetFunction implements Callable {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() < 2 || args.size() > 3) {
            throw new RuntimeException(MessageFormat.format("Expected 2 or 3 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof Map map)) {
            throw new RuntimeException("First argument must be an object");
        }

        Object key = args.get(1);
        Object defaultValue = args.size() == 3 ? args.get(2) : new NullValue();

        return map.getOrDefault(key, defaultValue);
    }
}
