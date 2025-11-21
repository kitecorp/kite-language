package io.kite.stdlib.functions.datetime;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

public class IsLeapYearFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.size() != 1) {
            throw new RuntimeException(MessageFormat.format("Expected 1 argument, got {0}", args.size()));
        }

        var arg = args.get(0);
        if (arg instanceof Number year) {
            return Year.isLeap(year.intValue());
        } else if (arg instanceof String dateStr) {
            try {
                var date = LocalDate.parse(dateStr);
                return Year.isLeap(date.getYear());
            } catch (Exception e) {
                throw new RuntimeException("Invalid date format: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Argument must be a year number or date string");
        }
    }
}
