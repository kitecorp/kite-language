package cloud.kitelang.stdlib.functions.string;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrimFunctionTest extends RuntimeTest {

    private final TrimFunction function = new TrimFunction();

    @Test
    void trimLeadingSpaces() {
        var res = function.call(interpreter, "  hello");
        assertEquals("hello", res);
    }

    @Test
    void trimTrailingSpaces() {
        var res = function.call(interpreter, "hello  ");
        assertEquals("hello", res);
    }

    @Test
    void trimBothSides() {
        var res = function.call(interpreter, "  hello  ");
        assertEquals("hello", res);
    }

    @Test
    void trimNoSpaces() {
        var res = function.call(interpreter, "hello");
        assertEquals("hello", res);
    }

    @Test
    void trimOnlySpaces() {
        var res = function.call(interpreter, "   ");
        assertEquals("", res);
    }

    @Test
    void trimTabs() {
        var res = function.call(interpreter, "\t\thello\t\t");
        assertEquals("hello", res);
    }

    @Test
    void trimNewlines() {
        var res = function.call(interpreter, "\n\nhello\n\n");
        assertEquals("hello", res);
    }

    @Test
    void trimEmpty() {
        var res = function.call(interpreter, "");
        assertEquals("", res);
    }

    @Test
    void trimPreservesInternal() {
        var res = function.call(interpreter, "  hello  world  ");
        assertEquals("hello  world", res);
    }

    @Test
    void trimTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", "world"));
    }

    @Test
    void trimInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }
}
