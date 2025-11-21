package io.kite.stdlib.functions.datetime;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ParseDateFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof String dateStr)) {
            throw new RuntimeException("First argument must be a date string");
        }
        if (!(args.get(1) instanceof String pattern)) {
            throw new RuntimeException("Second argument must be a format pattern");
        }

        try {
            var formatter = DateTimeFormatter.ofPattern(pattern);
            try {
                var date = LocalDate.parse(dateStr, formatter);
                return date.toString();
            } catch (Exception e) {
                var dateTime = LocalDateTime.parse(dateStr, formatter);
                return dateTime.toString();
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format or pattern: " + e.getMessage());
        }
    }
}
