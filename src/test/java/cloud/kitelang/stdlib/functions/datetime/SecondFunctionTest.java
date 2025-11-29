package cloud.kitelang.stdlib.functions.datetime;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled("Function 'second' removed due to namespace conflict with common resource names")
class SecondFunctionTest extends RuntimeTest {

    private final SecondFunction function = new SecondFunction();

    @Test
    void secondCurrentTime() {
        var res = function.call(interpreter);
        var expected = LocalTime.now().getSecond();
        assertEquals(expected, res);
    }

    @Test
    void secondFromDateTimeString() {
        var res = function.call(interpreter, "2024-12-25T14:30:45");
        assertEquals(45, res);
    }

    @Test
    void secondFromTimeString() {
        var res = function.call(interpreter, "09:45:30");
        assertEquals(30, res);
    }

    @Test
    void secondZero() {
        var res = function.call(interpreter, "12:00:00");
        assertEquals(0, res);
    }

    @Test
    void secondAlmostMinute() {
        var res = function.call(interpreter, "12:00:59");
        assertEquals(59, res);
    }

    @Test
    void secondMidpoint() {
        var res = function.call(interpreter, "12:00:30");
        assertEquals(30, res);
    }

    @Test
    void secondSingleDigit() {
        var res = function.call(interpreter, "12:00:05");
        assertEquals(5, res);
    }

    @Test
    void secondInvalidFormat() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "invalid-time"));
    }
}
