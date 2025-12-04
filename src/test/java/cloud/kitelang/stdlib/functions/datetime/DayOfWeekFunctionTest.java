package cloud.kitelang.stdlib.functions.datetime;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DayOfWeekFunctionTest extends RuntimeTest {

    private final DayOfWeekFunction function = new DayOfWeekFunction();

    @Test
    void dayOfWeekMonday() {
        var res = function.call(interpreter, "2025-01-06"); // Monday
        assertEquals(1, res);
    }

    @Test
    void dayOfWeekTuesday() {
        var res = function.call(interpreter, "2025-01-07"); // Tuesday
        assertEquals(2, res);
    }

    @Test
    void dayOfWeekWednesday() {
        var res = function.call(interpreter, "2025-01-08"); // Wednesday
        assertEquals(3, res);
    }

    @Test
    void dayOfWeekThursday() {
        var res = function.call(interpreter, "2025-01-09"); // Thursday
        assertEquals(4, res);
    }

    @Test
    void dayOfWeekFriday() {
        var res = function.call(interpreter, "2025-01-10"); // Friday
        assertEquals(5, res);
    }

    @Test
    void dayOfWeekSaturday() {
        var res = function.call(interpreter, "2025-01-11"); // Saturday
        assertEquals(6, res);
    }

    @Test
    void dayOfWeekSunday() {
        var res = function.call(interpreter, "2025-01-12"); // Sunday
        assertEquals(7, res);
    }

    @Test
    void dayOfWeekLeapYear() {
        var res = function.call(interpreter, "2024-02-29"); // Thursday
        assertEquals(4, res);
    }

    @Test
    void dayOfWeekTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }

    @Test
    void dayOfWeekInvalidDate() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "invalid-date"));
    }
}
