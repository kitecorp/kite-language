package cloud.kitelang.stdlib.functions.numeric;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class CeilFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() > 1) {
            throw new RuntimeException(MessageFormat.format("Too many arguments: {0}", args.size()));
        }
        var arg = args.get(0);
        if (arg instanceof Number number) {
            return (int) Math.ceil(number.doubleValue());
        } else {
            throw new RuntimeException("Invalid argument");
        }
    }
}
