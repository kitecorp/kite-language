package cloud.kitelang.stdlib.functions.datetime;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FormatDateFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }
        var dateArg = args.get(0);
        var formatArg = args.get(1);

        if (!(dateArg instanceof String dateStr)) {
            throw new RuntimeException("First argument must be a date string");
        }
        if (!(formatArg instanceof String pattern)) {
            throw new RuntimeException("Second argument must be a format pattern string");
        }

        try {
            var formatter = DateTimeFormatter.ofPattern(pattern);
            try {
                var date = LocalDate.parse(dateStr);
                return date.format(formatter);
            } catch (Exception e) {
                var dateTime = LocalDateTime.parse(dateStr);
                return dateTime.format(formatter);
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format or pattern: " + e.getMessage());
        }
    }
}
