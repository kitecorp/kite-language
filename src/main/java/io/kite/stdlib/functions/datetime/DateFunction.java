package io.kite.stdlib.functions.datetime;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.time.LocalDate;
import java.util.List;

public class DateFunction implements Callable {
    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return LocalDate.now().toString();
    }
}
