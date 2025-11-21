package io.kite.stdlib.functions.datetime;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DiffDaysFunctionTest extends RuntimeTest {

    private final DiffDaysFunction function = new DiffDaysFunction();

    @Test
    void diffDaysPositive() {
        var res = function.call(interpreter, "2025-01-01", "2025-01-06");
        assertEquals(5L, res);
    }

    @Test
    void diffDaysNegative() {
        var res = function.call(interpreter, "2025-01-10", "2025-01-05");
        assertEquals(-5L, res);
    }

    @Test
    void diffDaysZero() {
        var res = function.call(interpreter, "2025-01-01", "2025-01-01");
        assertEquals(0L, res);
    }

    @Test
    void diffDaysCrossMonth() {
        var res = function.call(interpreter, "2025-01-28", "2025-02-05");
        assertEquals(8L, res);
    }

    @Test
    void diffDaysCrossYear() {
        var res = function.call(interpreter, "2024-12-30", "2025-01-05");
        assertEquals(6L, res);
    }

    @Test
    void diffDaysLeapYear() {
        var res = function.call(interpreter, "2024-02-28", "2024-03-01");
        assertEquals(2L, res); // 2024 is a leap year
    }

    @Test
    void diffDaysLargeGap() {
        var res = function.call(interpreter, "2025-01-01", "2026-01-01");
        assertEquals(365L, res);
    }

    @Test
    void diffDaysTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "2025-01-01"));
    }

    @Test
    void diffDaysInvalidFirstDate() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "invalid-date", "2025-01-01"));
    }

    @Test
    void diffDaysInvalidSecondDate() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "2025-01-01", "invalid-date"));
    }
}
