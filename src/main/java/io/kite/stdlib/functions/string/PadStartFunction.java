package io.kite.stdlib.functions.string;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.util.List;

public class PadStartFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() < 2 || args.size() > 3) {
            throw new RuntimeException(MessageFormat.format("Expected 2 or 3 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof String str)) {
            throw new RuntimeException("First argument must be a string");
        }
        if (!(args.get(1) instanceof Number length)) {
            throw new RuntimeException("Second argument must be a number");
        }

        String padChar = args.size() == 3 ? args.get(2).toString() : " ";
        if (padChar.isEmpty()) {
            padChar = " ";
        }

        int targetLength = length.intValue();
        if (str.length() >= targetLength) {
            return str;
        }

        int padLength = targetLength - str.length();
        String padding = padChar.repeat((int) Math.ceil((double) padLength / padChar.length()));
        return padding.substring(0, padLength) + str;
    }
}
