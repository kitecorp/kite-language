package cloud.kitelang.stdlib.functions.datetime;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ToISOStringFunctionTest extends RuntimeTest {

    private final ToISOStringFunction function = new ToISOStringFunction();

    @Test
    void toISOStringDate() {
        var res = function.call(interpreter, "2025-01-15");
        assertEquals("2025-01-15", res);
    }

    @Test
    void toISOStringDateTime() {
        var res = function.call(interpreter, "2025-01-15T14:30:00");
        assertEquals("2025-01-15T14:30:00", res);
    }

    @Test
    void toISOStringDateTimeWithSeconds() {
        var res = function.call(interpreter, "2025-01-15T14:30:45");
        assertEquals("2025-01-15T14:30:45", res);
    }

    @Test
    void toISOStringMidnight() {
        var res = function.call(interpreter, "2025-01-15T00:00:00");
        assertEquals("2025-01-15T00:00:00", res);
    }

    @Test
    void toISOStringNoon() {
        var res = function.call(interpreter, "2025-01-15T12:00:00");
        assertEquals("2025-01-15T12:00:00", res);
    }

    @Test
    void toISOStringLeapYear() {
        var res = function.call(interpreter, "2024-02-29");
        assertEquals("2024-02-29", res);
    }

    @Test
    void toISOStringTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }

    @Test
    void toISOStringInvalidDate() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "invalid-date"));
    }

    @Test
    void toISOStringInvalidDateTime() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "2025-01-15T25:00:00"));
    }
}
