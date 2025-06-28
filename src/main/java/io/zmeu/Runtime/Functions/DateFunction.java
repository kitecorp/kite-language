package io.zmeu.Runtime.Functions;

import io.zmeu.Runtime.Callable;
import io.zmeu.Runtime.Interpreter;

import java.time.LocalDate;
import java.util.List;

public class DateFunction implements Callable {
    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return LocalDate.now().toString();
    }
}
