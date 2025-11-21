package io.kite.stdlib.functions.datetime;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DiffDaysFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 2) {
            throw new RuntimeException(MessageFormat.format("Expected 2 arguments, got {0}", args.size()));
        }

        if (!(args.get(0) instanceof String date1Str)) {
            throw new RuntimeException("First argument must be a date string");
        }
        if (!(args.get(1) instanceof String date2Str)) {
            throw new RuntimeException("Second argument must be a date string");
        }

        try {
            var date1 = LocalDate.parse(date1Str);
            var date2 = LocalDate.parse(date2Str);
            return ChronoUnit.DAYS.between(date1, date2);
        } catch (Exception e) {
            throw new RuntimeException("Invalid date format: " + e.getMessage());
        }
    }
}
