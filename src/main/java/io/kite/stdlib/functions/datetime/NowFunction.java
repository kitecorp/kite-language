package io.kite.stdlib.functions.datetime;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.time.LocalDateTime;
import java.util.List;

public class NowFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return LocalDateTime.now().toString();
    }
}
