package io.kite.stdlib.functions.numeric;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.util.List;

public class RandomFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return Math.random();
    }
}
