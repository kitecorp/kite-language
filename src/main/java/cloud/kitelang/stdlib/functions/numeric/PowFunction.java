package cloud.kitelang.stdlib.functions.numeric;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class PowFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() > 2) {
            throw new RuntimeException(MessageFormat.format("Too many arguments: {0}", args.size()));
        }
        if (args.size() < 2) {
            throw new RuntimeException(MessageFormat.format("Too few arguments: {0} but required at least 2", args.size()));
        }
        var base = args.get(0);
        var exponent = args.get(1);
        if (base instanceof Number b && exponent instanceof Number ex) {
            return (int) Math.pow(b.doubleValue(), ex.doubleValue());
        } else {
            throw new RuntimeException("Invalid argument");
        }
    }
}
