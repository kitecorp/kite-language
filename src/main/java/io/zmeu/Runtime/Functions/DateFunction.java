package io.kite.Runtime.Functions;

import io.kite.Runtime.Callable;
import io.kite.Runtime.Interpreter;

import java.time.LocalDate;
import java.util.List;

public class DateFunction implements Callable {
    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return LocalDate.now().toString();
    }
}
