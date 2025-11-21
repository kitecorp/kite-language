package io.kite.stdlib.functions.datetime;

import io.kite.execution.Callable;
import io.kite.execution.Interpreter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class HourFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.isEmpty()) {
            return LocalTime.now().getHour();
        }
        var arg = args.get(0);
        if (arg instanceof String timeStr) {
            try {
                return LocalDateTime.parse(timeStr).getHour();
            } catch (Exception e) {
                try {
                    return LocalTime.parse(timeStr).getHour();
                } catch (Exception ex) {
                    throw new RuntimeException("Invalid time format");
                }
            }
        }
        throw new RuntimeException("Argument must be a time string");
    }
}
