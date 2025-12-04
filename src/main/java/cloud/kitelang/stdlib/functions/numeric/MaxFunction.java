package cloud.kitelang.stdlib.functions.numeric;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MaxFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        ArrayList<Comparable> arg = new ArrayList<>(args.size());
        for (var it : args) {
            if (it instanceof Number number){
                arg.add((Comparable) number);
            } else {
                throw new RuntimeException("Invalid argument");
            }
        }
        return Collections.max(arg);
    }
}
