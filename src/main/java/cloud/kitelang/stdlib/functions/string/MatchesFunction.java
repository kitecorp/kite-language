package cloud.kitelang.stdlib.functions.string;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;
import java.util.regex.PatternSyntaxException;

public class MatchesFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof String str)) {
            throw new RuntimeException("First argument must be a string");
        }
        if (!(args.get(1) instanceof String pattern)) {
            throw new RuntimeException("Second argument must be a string (regex pattern)");
        }

        try {
            return str.matches(pattern);
        } catch (PatternSyntaxException e) {
            throw new RuntimeException("Invalid regex pattern: " + e.getMessage());
        }
    }
}
