package io.kite.stdlib.functions.datetime;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DayFunctionTest extends RuntimeTest {

    private final DayFunction function = new DayFunction();

    @Test
    void dayCurrentDate() {
        var res = function.call(interpreter);
        assertEquals(LocalDate.now().getDayOfMonth(), res);
    }

    @Test
    void dayFromDateString() {
        var res = function.call(interpreter, "2024-12-25");
        assertEquals(25, res);
    }

    @Test
    void dayFromDateTimeString() {
        var res = function.call(interpreter, "2024-01-15T10:30:00");
        assertEquals(15, res);
    }

    @Test
    void dayFirstOfMonth() {
        var res = function.call(interpreter, "2024-01-01");
        assertEquals(1, res);
    }

    @Test
    void dayLastOfMonth() {
        var res = function.call(interpreter, "2024-12-31");
        assertEquals(31, res);
    }

    @Test
    void dayMidMonth() {
        var res = function.call(interpreter, "2024-06-15");
        assertEquals(15, res);
    }

    @Test
    void dayFebruary() {
        var res = function.call(interpreter, "2024-02-29");
        assertEquals(29, res);
    }

    @Test
    void dayInvalidFormat() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "invalid-date"));
    }
}
