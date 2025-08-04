package io.kite.Runtime.Functions.Cast;

import io.kite.Runtime.Callable;
import io.kite.Runtime.Interpreter;

import java.util.List;

public class BooleanCastFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() == 0) {
            return false;
        }
        Object arg = args.get(0);
        if (arg == null) {
            return false;
        }
        if (arg instanceof String s) {
            if ("yes".equalsIgnoreCase(s)) {
                return true;
            } else if ("no".equalsIgnoreCase(s)) {
                return false;
            }
            return Boolean.parseBoolean(s);
        }
        throw new RuntimeException("Argument '%s' can't be converted to int".formatted(arg));
    }
}
