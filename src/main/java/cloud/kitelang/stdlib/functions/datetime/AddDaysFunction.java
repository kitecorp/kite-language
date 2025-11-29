package cloud.kitelang.stdlib.functions.datetime;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;

public class AddDaysFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof String dateStr)) {
            throw new RuntimeException("First argument must be a date string");
        }
        if (!(args.get(1) instanceof Number days)) {
            throw new RuntimeException("Second argument must be a number");
        }

        try {
            var date = LocalDate.parse(dateStr);
            return date.plusDays(days.longValue()).toString();
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format: " + e.getMessage());
        }
    }
}
