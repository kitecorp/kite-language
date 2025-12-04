package cloud.kitelang.stdlib.functions.collections;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class SliceFunction implements Callable {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() < 2 || args.size() > 3) {
            throw new RuntimeException(MessageFormat.format("Expected 2 or 3 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof List list)) {
            throw new RuntimeException("First argument must be an array");
        }
        if (!(args.get(1) instanceof Number start)) {
            throw new RuntimeException("Second argument must be a number");
        }

        int startIdx = start.intValue();
        int endIdx = args.size() == 3 ?
                ((Number) args.get(2)).intValue() :
                list.size();

        return list.subList(Math.max(0, startIdx), Math.min(list.size(), endIdx));
    }
}
