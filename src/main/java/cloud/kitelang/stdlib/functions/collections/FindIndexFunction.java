package cloud.kitelang.stdlib.functions.collections;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class FindIndexFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof List<?> list)) {
            throw new RuntimeException("First argument must be an array");
        }

        var searchValue = args.get(1);

        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (item != null && item.equals(searchValue)) {
                return i;
            }
        }

        return -1;
    }
}
