package cloud.kitelang.stdlib.functions.numeric;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.util.List;

public class RandomFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        return Math.random();
    }
}
