package cloud.kitelang.stdlib.functions.datetime;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MonthFunctionTest extends RuntimeTest {

    private final MonthFunction function = new MonthFunction();

    @Test
    void monthCurrentDate() {
        var res = function.call(interpreter);
        assertEquals(LocalDate.now().getMonthValue(), res);
    }

    @Test
    void monthFromDateString() {
        var res = function.call(interpreter, "2024-12-25");
        assertEquals(12, res);
    }

    @Test
    void monthFromDateTimeString() {
        var res = function.call(interpreter, "2024-01-15T10:30:00");
        assertEquals(1, res);
    }

    @Test
    void monthJanuary() {
        var res = function.call(interpreter, "2024-01-01");
        assertEquals(1, res);
    }

    @Test
    void monthDecember() {
        var res = function.call(interpreter, "2024-12-31");
        assertEquals(12, res);
    }

    @Test
    void monthMidYear() {
        var res = function.call(interpreter, "2024-06-15");
        assertEquals(6, res);
    }

    @Test
    void monthInvalidFormat() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "invalid-date"));
    }
}
