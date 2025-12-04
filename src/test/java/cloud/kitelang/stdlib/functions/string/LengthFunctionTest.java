package cloud.kitelang.stdlib.functions.string;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LengthFunctionTest extends RuntimeTest {

    private final LengthFunction function = new LengthFunction();

    @Test
    void lengthOfString() {
        var res = function.call(interpreter, "hello");
        assertEquals(5, res);
    }

    @Test
    void lengthOfEmptyString() {
        var res = function.call(interpreter, "");
        assertEquals(0, res);
    }

    @Test
    void lengthOfArray() {
        var res = function.call(interpreter, List.of(List.of(1, 2, 3)));
        assertEquals(3, res);
    }

    @Test
    void lengthOfEmptyArray() {
        var res = function.call(interpreter, List.of(List.of()));
        assertEquals(0, res);
    }

    @Test
    void lengthTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", "world"));
    }

    @Test
    void lengthInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }
}
