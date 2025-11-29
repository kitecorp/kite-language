package cloud.kitelang.stdlib.functions.string;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PadStartFunctionTest extends RuntimeTest {

    private final PadStartFunction function = new PadStartFunction();

    @Test
    void padStartBasic() {
        var res = function.call(interpreter, "5", 3);
        assertEquals("  5", res);
    }

    @Test
    void padStartWithChar() {
        var res = function.call(interpreter, "5", 3, "0");
        assertEquals("005", res);
    }

    @Test
    void padStartAlreadyLong() {
        var res = function.call(interpreter, "hello", 3);
        assertEquals("hello", res);
    }

    @Test
    void padStartExactLength() {
        var res = function.call(interpreter, "abc", 3);
        assertEquals("abc", res);
    }

    @Test
    void padStartCustomChar() {
        var res = function.call(interpreter, "world", 10, "*");
        assertEquals("*****world", res);
    }

    @Test
    void padStartZeroLength() {
        var res = function.call(interpreter, "hello", 0);
        assertEquals("hello", res);
    }

    @Test
    void padStartEmptyString() {
        var res = function.call(interpreter, "", 5);
        assertEquals("     ", res);
    }

    @Test
    void padStartEmptyStringWithChar() {
        var res = function.call(interpreter, "", 5, "x");
        assertEquals("xxxxx", res);
    }

    @Test
    void padStartTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello"));
    }

    @Test
    void padStartInvalidFirstArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123, 5));
    }

    @Test
    void padStartInvalidSecondArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", "not-a-number"));
    }
}
