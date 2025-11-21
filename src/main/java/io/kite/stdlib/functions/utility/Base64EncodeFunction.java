package io.kite.stdlib.functions.utility;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.Base64;
import java.util.List;

public class Base64EncodeFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof String str)) {
            throw new RuntimeException("Argument must be a string");
        }

        return Base64.getEncoder().encodeToString(str.getBytes());
    }
}
