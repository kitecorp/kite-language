package cloud.kitelang.stdlib.functions.cast;

import cloud.kitelang.base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
class StringCastFunctionTest extends RuntimeTest {
    private final StringCastFunction function = new StringCastFunction();

    @Test
    void castIntToString() {
        var res = function.call(interpreter, 3);
        assertEquals("3", res);
    }

    @Test
    void castDoubleToString() {
        var res = function.call(interpreter, 3.2d);
        assertEquals("3.2", res);
    }

    @Test
    void castFloatToString() {
        var res = function.call(interpreter, 3.2f);
        assertEquals("3.2", res);
    }

    @Test
    void castBoolToString() {
        var res = function.call(interpreter, true);
        assertEquals("true", res);
    }

    @Test
    void ceilMaxArgs() {
        Assertions.assertThrows(RuntimeException.class, () -> function.call(interpreter, 2.0d, 3.0d, 1.0d));
    }

}