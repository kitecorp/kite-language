package io.kite.stdlib.functions.datetime;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ToISOStringFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof String dateStr)) {
            throw new RuntimeException("Argument must be a date string");
        }

        try {
            try {
                var date = LocalDate.parse(dateStr);
                return date.format(DateTimeFormatter.ISO_DATE);
            } catch (Exception e) {
                var dateTime = LocalDateTime.parse(dateStr);
                return dateTime.format(DateTimeFormatter.ISO_DATE_TIME);
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format: " + e.getMessage());
        }
    }
}
