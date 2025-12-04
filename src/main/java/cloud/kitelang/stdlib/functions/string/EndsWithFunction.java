package cloud.kitelang.stdlib.functions.string;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class EndsWithFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }
        var str = args.get(0);
        if (!(str instanceof String s)) {
            throw new RuntimeException("First argument must be a string");
        }
        var suffix = args.get(1);
        if (!(suffix instanceof String suf)) {
            throw new RuntimeException("Second argument must be a string");
        }
        return s.endsWith(suf);
    }
}
