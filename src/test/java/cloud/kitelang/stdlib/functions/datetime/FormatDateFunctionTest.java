package cloud.kitelang.stdlib.functions.datetime;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormatDateFunctionTest extends RuntimeTest {

    private final FormatDateFunction function = new FormatDateFunction();

    @Test
    void formatDateBasic() {
        var res = function.call(interpreter, "2024-12-25", "yyyy-MM-dd");
        assertEquals("2024-12-25", res);
    }

    @Test
    void formatDateCustomPattern() {
        var res = function.call(interpreter, "2024-12-25", "dd/MM/yyyy");
        assertEquals("25/12/2024", res);
    }

    @Test
    void formatDateTimeWithTime() {
        var res = function.call(interpreter, "2024-12-25T10:30:00", "yyyy-MM-dd HH:mm");
        assertEquals("2024-12-25 10:30", res);
    }

    @Test
    void formatDateInvalidPattern() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "2024-12-25", "invalid"));
    }

    @Test
    void formatDateInvalidArgCount() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "2024-12-25"));
    }
}
