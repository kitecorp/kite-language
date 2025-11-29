package cloud.kitelang.stdlib.functions.utility;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.util.List;
import java.util.UUID;

public class UuidFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return UUID.randomUUID().toString();
    }
}
