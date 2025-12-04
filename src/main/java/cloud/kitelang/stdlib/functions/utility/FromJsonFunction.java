package cloud.kitelang.stdlib.functions.utility;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class FromJsonFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof String json)) {
            throw new RuntimeException("Argument must be a JSON string");
        }

        // TODO: Implement JSON parsing or integrate with Jackson when available
        throw new RuntimeException("fromJson() not yet implemented - JSON library integration pending");
    }
}
