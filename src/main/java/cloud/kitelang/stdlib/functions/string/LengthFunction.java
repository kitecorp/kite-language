package cloud.kitelang.stdlib.functions.string;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;
import cloud.kitelang.execution.values.DeferredFunctionCall;
import cloud.kitelang.execution.values.DeferredValue;

import java.text.MessageFormat;
import java.util.List;

public class LengthFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }
        var arg = args.get(0);

        // Propagate deferred values - actual computation happens at apply time
        if (arg instanceof DeferredValue deferred) {
            return new DeferredFunctionCall("length", deferred);
        }

        if (arg instanceof String str) {
            return str.length();
        } else if (arg instanceof List<?> list) {
            return list.size();
        } else {
            throw new RuntimeException("Argument must be a string or array");
        }
    }
}
