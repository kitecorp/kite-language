package cloud.kitelang.stdlib.functions.numeric;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class SqrtFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }
        var arg = args.get(0);
        if (arg instanceof Number n) {
            return Math.sqrt(n.doubleValue());
        } else {
            throw new RuntimeException("Argument must be a number");
        }
    }
}
