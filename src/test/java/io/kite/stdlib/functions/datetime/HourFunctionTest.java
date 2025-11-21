package io.kite.stdlib.functions.datetime;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HourFunctionTest extends RuntimeTest {

    private final HourFunction function = new HourFunction();

    @Test
    void hourCurrentTime() {
        var res = function.call(interpreter);
        var expected = LocalTime.now().getHour();
        assertEquals(expected, res);
    }

    @Test
    void hourFromDateTimeString() {
        var res = function.call(interpreter, "2024-12-25T14:30:00");
        assertEquals(14, res);
    }

    @Test
    void hourFromTimeString() {
        var res = function.call(interpreter, "09:45:30");
        assertEquals(9, res);
    }

    @Test
    void hourMidnight() {
        var res = function.call(interpreter, "00:00:00");
        assertEquals(0, res);
    }

    @Test
    void hourAlmostMidnight() {
        var res = function.call(interpreter, "23:59:59");
        assertEquals(23, res);
    }

    @Test
    void hourNoon() {
        var res = function.call(interpreter, "12:00:00");
        assertEquals(12, res);
    }

    @Test
    void hourInvalidFormat() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "invalid-time"));
    }
}
