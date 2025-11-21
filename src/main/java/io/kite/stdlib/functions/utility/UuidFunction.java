package io.kite.stdlib.functions.utility;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.util.List;
import java.util.UUID;

public class UuidFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return UUID.randomUUID().toString();
    }
}
