package io.kite.stdlib.functions.string;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class IndexOfFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }
        var str = args.get(0);
        if (!(str instanceof String s)) {
            throw new RuntimeException("First argument must be a string");
        }
        var search = args.get(1);
        if (!(search instanceof String searchStr)) {
            throw new RuntimeException("Second argument must be a string");
        }
        return s.indexOf(searchStr);
    }
}
