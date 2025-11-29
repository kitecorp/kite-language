package cloud.kitelang.stdlib.functions.datetime;

import cloud.kitelang.execution.Callable;
import cloud.kitelang.execution.Interpreter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class MinuteFunction implements Callable {

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        if (args.isEmpty()) {
            return LocalTime.now().getMinute();
        }
        var arg = args.get(0);
        if (arg instanceof String timeStr) {
            try {
                return LocalDateTime.parse(timeStr).getMinute();
            } catch (Exception e) {
                try {
                    return LocalTime.parse(timeStr).getMinute();
                } catch (Exception ex) {
                    throw new RuntimeException("Invalid time format");
                }
            }
        }
        throw new RuntimeException("Argument must be a time string");
    }
}
