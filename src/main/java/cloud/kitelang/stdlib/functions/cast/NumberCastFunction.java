package cloud.kitelang.stdlib.functions.cast;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.util.List;

public class NumberCastFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Object arg = args.get(0);
        if (arg instanceof String s) {
            return Integer.parseInt(s);
        } else if (arg instanceof Number d) {
            return d.intValue();
        } else if (arg instanceof Double d) {
            return d.doubleValue();
        } else if (arg instanceof Float d) {
            return d.floatValue();
        }
        throw new RuntimeException("Argument '%s' can't be converted to int".formatted(arg));
    }
}
