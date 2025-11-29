package cloud.kitelang.stdlib.functions.datetime;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinuteFunctionTest extends RuntimeTest {

    private final MinuteFunction function = new MinuteFunction();

    @Test
    void minuteCurrentTime() {
        var res = function.call(interpreter);
        var expected = LocalTime.now().getMinute();
        assertEquals(expected, res);
    }

    @Test
    void minuteFromDateTimeString() {
        var res = function.call(interpreter, "2024-12-25T14:30:00");
        assertEquals(30, res);
    }

    @Test
    void minuteFromTimeString() {
        var res = function.call(interpreter, "09:45:30");
        assertEquals(45, res);
    }

    @Test
    void minuteZero() {
        var res = function.call(interpreter, "12:00:00");
        assertEquals(0, res);
    }

    @Test
    void minuteAlmostHour() {
        var res = function.call(interpreter, "12:59:00");
        assertEquals(59, res);
    }

    @Test
    void minuteMidpoint() {
        var res = function.call(interpreter, "12:30:00");
        assertEquals(30, res);
    }

    @Test
    void minuteInvalidFormat() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "invalid-time"));
    }
}
