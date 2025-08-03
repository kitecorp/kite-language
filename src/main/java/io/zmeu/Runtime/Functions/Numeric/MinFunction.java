package io.kite.Runtime.Functions.Numeric;

import io.kite.Runtime.Callable;
import io.kite.Runtime.Interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinFunction implements Callable {

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
        return Collections.min(arg);
    }
}
