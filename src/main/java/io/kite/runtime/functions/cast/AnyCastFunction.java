package io.kite.runtime.functions.cast;

import io.kite.runtime.Callable;
import io.kite.runtime.Interpreter;

import java.util.List;

public class AnyCastFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return args;
    }
}
