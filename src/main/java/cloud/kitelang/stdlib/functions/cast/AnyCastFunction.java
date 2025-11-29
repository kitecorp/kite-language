package cloud.kitelang.stdlib.functions.cast;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.util.List;

public class AnyCastFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return args;
    }
}
