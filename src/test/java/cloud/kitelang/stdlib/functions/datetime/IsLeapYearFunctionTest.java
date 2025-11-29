package cloud.kitelang.stdlib.functions.datetime;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IsLeapYearFunctionTest extends RuntimeTest {

    private final IsLeapYearFunction function = new IsLeapYearFunction();

    @Test
    void isLeapYearTrue() {
        var res = function.call(interpreter, 2024);
        assertEquals(true, res);
    }

    @Test
    void isLeapYearFalse() {
        var res = function.call(interpreter, 2025);
        assertEquals(false, res);
    }

    @Test
    void isLeapYearCentury() {
        var res1 = function.call(interpreter, 2000); // Divisible by 400
        var res2 = function.call(interpreter, 1900); // Divisible by 100 but not 400
        assertEquals(true, res1);
        assertEquals(false, res2);
    }

    @Test
    void isLeapYearFromDate() {
        var res = function.call(interpreter, "2024-02-29");
        assertEquals(true, res);
    }

    @Test
    void isLeapYearFromDateNonLeap() {
        var res = function.call(interpreter, "2025-06-15");
        assertEquals(false, res);
    }

    @Test
    void isLeapYearOldYear() {
        var res = function.call(interpreter, 2020);
        assertEquals(true, res);
    }

    @Test
    void isLeapYearFutureYear() {
        var res = function.call(interpreter, 2028);
        assertEquals(true, res);
    }

    @Test
    void isLeapYearTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }

    @Test
    void isLeapYearInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-a-year-or-date"));
    }
}
