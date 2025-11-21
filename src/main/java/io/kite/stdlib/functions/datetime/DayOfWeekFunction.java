package io.kite.stdlib.functions.datetime;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;

public class DayOfWeekFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.isEmpty() || args.size() > 1) {
            throw new RuntimeException(MessageFormat.format("Expected 0 or 1 arguments, got {0}", args.size()));
        }

        try {
            LocalDate date;
            if (args.isEmpty()) {
                date = LocalDate.now();
            } else if (args.get(0) instanceof String dateStr) {
                date = LocalDate.parse(dateStr);
            } else {
                throw new RuntimeException("Argument must be a date string");
            }

            return date.getDayOfWeek().getValue(); // Monday=1, Sunday=7
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format: " + e.getMessage());
        }
    }
}
