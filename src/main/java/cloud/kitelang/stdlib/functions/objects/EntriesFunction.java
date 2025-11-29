package cloud.kitelang.stdlib.functions.objects;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EntriesFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof Map<?, ?> map)) {
            throw new RuntimeException("Argument must be an object");
        }

        var result = new ArrayList<List<Object>>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            result.add(List.of(entry.getKey(), entry.getValue()));
        }
        return result;
    }
}
