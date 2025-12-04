package cloud.kitelang.stdlib.functions.datetime;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.util.List;

public class TimestampFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return System.currentTimeMillis();
    }
}
