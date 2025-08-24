package io.kite.Runtime.Functions.Cast;

import io.kite.Runtime.Callable;
import io.kite.Runtime.Interpreter;

import java.util.List;

public class AnyCastFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return args;
    }
}
