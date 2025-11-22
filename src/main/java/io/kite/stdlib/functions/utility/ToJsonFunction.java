package io.kite.stdlib.functions.utility;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class ToJsonFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }

        // TODO: Implement JSON stringification or integrate with Jackson when available
        throw new RuntimeException("toJson() not yet implemented - JSON library integration pending");
    }
}
