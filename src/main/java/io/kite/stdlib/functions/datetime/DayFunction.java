package io.kite.stdlib.functions.datetime;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DayFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.isEmpty()) {
            return LocalDate.now().getDayOfMonth();
        }
        var arg = args.get(0);
        if (arg instanceof String dateStr) {
            try {
                return LocalDate.parse(dateStr).getDayOfMonth();
            } catch (Exception e) {
                try {
                    return LocalDateTime.parse(dateStr).getDayOfMonth();
                } catch (Exception ex) {
                    throw new RuntimeException("Invalid date format");
                }
            }
        }
        throw new RuntimeException("Argument must be a date string");
    }
}
