package cloud.kitelang.stdlib.functions.string;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubstringFunctionTest extends RuntimeTest {

    private final SubstringFunction function = new SubstringFunction();

    @Test
    void substringFromStart() {
        var res = function.call(interpreter, "hello world", 0, 5);
        assertEquals("hello", res);
    }

    @Test
    void substringFromMiddle() {
        var res = function.call(interpreter, "hello world", 6, 11);
        assertEquals("world", res);
    }

    @Test
    void substringToEnd() {
        var res = function.call(interpreter, "hello world", 6);
        assertEquals("world", res);
    }

    @Test
    void substringZeroLength() {
        var res = function.call(interpreter, "hello", 2, 2);
        assertEquals("", res);
    }

    @Test
    void substringInvalidArgCount() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello"));
    }

    @Test
    void substringInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123, 0, 5));
    }
}
