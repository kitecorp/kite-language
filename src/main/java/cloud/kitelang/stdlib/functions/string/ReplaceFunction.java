package cloud.kitelang.stdlib.functions.string;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class ReplaceFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 3) {
            throw new RuntimeException(MessageFormat.format("Expected 3 arguments, got {0}", args.size()));
        }
        var str = args.get(0);
        if (!(str instanceof String s)) {
            throw new RuntimeException("First argument must be a string");
        }
        var target = args.get(1);
        if (!(target instanceof String t)) {
            throw new RuntimeException("Second argument must be a string");
        }
        var replacement = args.get(2);
        if (!(replacement instanceof String r)) {
            throw new RuntimeException("Third argument must be a string");
        }
        return s.replace(t, r);
    }
}
