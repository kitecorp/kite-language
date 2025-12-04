package cloud.kitelang.stdlib.functions.collections;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class ContainsFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }
        var collection = args.get(0);
        var element = args.get(1);

        if (collection instanceof String str) {
            if (element instanceof String search) {
                return str.contains(search);
            }
            throw new RuntimeException("Second argument must be a string when first is a string");
        } else if (collection instanceof List<?> list) {
            return list.contains(element);
        } else {
            throw new RuntimeException("First argument must be a string or array");
        }
    }
}
