package cloud.kitelang.stdlib.functions.datetime;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YearFunctionTest extends RuntimeTest {

    private final YearFunction function = new YearFunction();

    @Test
    void yearCurrentDate() {
        var res = function.call(interpreter);
        assertEquals(LocalDate.now().getYear(), res);
    }

    @Test
    void yearFromDateString() {
        var res = function.call(interpreter, "2024-12-25");
        assertEquals(2024, res);
    }

    @Test
    void yearFromDateTimeString() {
        var res = function.call(interpreter, "2024-12-25T10:30:00");
        assertEquals(2024, res);
    }

    @Test
    void yearInvalidFormat() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "invalid-date"));
    }
}
