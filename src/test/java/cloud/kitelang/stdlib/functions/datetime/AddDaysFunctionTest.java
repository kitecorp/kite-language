package cloud.kitelang.stdlib.functions.datetime;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AddDaysFunctionTest extends RuntimeTest {

    private final AddDaysFunction function = new AddDaysFunction();

    @Test
    void addDaysPositive() {
        var res = function.call(interpreter, "2025-01-01", 5);
        assertEquals("2025-01-06", res);
    }

    @Test
    void addDaysNegative() {
        var res = function.call(interpreter, "2025-01-15", -5);
        assertEquals("2025-01-10", res);
    }

    @Test
    void addDaysZero() {
        var res = function.call(interpreter, "2025-01-01", 0);
        assertEquals("2025-01-01", res);
    }

    @Test
    void addDaysCrossMonth() {
        var res = function.call(interpreter, "2025-01-28", 5);
        assertEquals("2025-02-02", res);
    }

    @Test
    void addDaysCrossYear() {
        var res = function.call(interpreter, "2024-12-30", 5);
        assertEquals("2025-01-04", res);
    }

    @Test
    void addDaysLeapYear() {
        var res = function.call(interpreter, "2024-02-28", 1);
        assertEquals("2024-02-29", res);
    }

    @Test
    void addDaysLargeValue() {
        var res = function.call(interpreter, "2025-01-01", 365);
        assertEquals("2026-01-01", res);
    }

    @Test
    void addDaysTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "2025-01-01"));
    }

    @Test
    void addDaysInvalidDate() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "invalid-date", 5));
    }

    @Test
    void addDaysInvalidDays() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "2025-01-01", "not-a-number"));
    }
}
