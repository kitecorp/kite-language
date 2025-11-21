package io.kite.runtime.functions;

import io.kite.runtime.Callable;
import io.kite.runtime.Interpreter;

import java.time.LocalDate;
import java.util.List;

public class DateFunction implements Callable {
    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return LocalDate.now().toString();
    }
}
