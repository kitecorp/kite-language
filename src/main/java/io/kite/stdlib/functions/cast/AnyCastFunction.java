package io.kite.stdlib.functions.cast;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.util.List;

public class AnyCastFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return args;
    }
}
