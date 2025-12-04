package cloud.kitelang.stdlib.functions.collections;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class SumFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof List<?> list)) {
            throw new RuntimeException("Argument must be an array");
        }

        double sum = 0;
        for (Object item : list) {
            if (item instanceof Number n) {
                sum += n.doubleValue();
            } else {
                throw new RuntimeException("All elements must be numbers");
            }
        }
        return sum;
    }
}
