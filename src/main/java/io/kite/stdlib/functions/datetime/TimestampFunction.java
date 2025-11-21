package io.kite.stdlib.functions.datetime;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.util.List;

public class TimestampFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return System.currentTimeMillis();
    }
}
