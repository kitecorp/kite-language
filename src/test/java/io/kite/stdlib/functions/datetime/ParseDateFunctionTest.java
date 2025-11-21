package io.kite.stdlib.functions.datetime;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParseDateFunctionTest extends RuntimeTest {

    private final ParseDateFunction function = new ParseDateFunction();

    @Test
    void parseDateISOFormat() {
        var res = function.call(interpreter, "2025-01-15", "yyyy-MM-dd");
        assertEquals("2025-01-15", res);
    }

    @Test
    void parseDateCustomFormat() {
        var res = function.call(interpreter, "15/01/2025", "dd/MM/yyyy");
        assertEquals("2025-01-15", res);
    }

    @Test
    void parseDateUSFormat() {
        var res = function.call(interpreter, "01/15/2025", "MM/dd/yyyy");
        assertEquals("2025-01-15", res);
    }

    @Test
    void parseDateCompactFormat() {
        var res = function.call(interpreter, "20250115", "yyyyMMdd");
        assertEquals("2025-01-15", res);
    }

    @Test
    void parseDateWithDashes() {
        var res = function.call(interpreter, "15-01-2025", "dd-MM-yyyy");
        assertEquals("2025-01-15", res);
    }

    @Test
    void parseDateLeapYear() {
        var res = function.call(interpreter, "29/02/2024", "dd/MM/yyyy");
        assertEquals("2024-02-29", res);
    }

    @Test
    void parseDateTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }

    @Test
    void parseDateInvalidFormat() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-a-date", "yyyy-MM-dd"));
    }

    @Test
    void parseDateMismatchedFormat() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "2025-01-15", "dd/MM/yyyy"));
    }
}
