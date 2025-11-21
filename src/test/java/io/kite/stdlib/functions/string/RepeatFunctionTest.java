package io.kite.stdlib.functions.string;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepeatFunctionTest extends RuntimeTest {

    private final RepeatFunction function = new RepeatFunction();

    @Test
    void repeatBasic() {
        var res = function.call(interpreter, "ab", 3);
        assertEquals("ababab", res);
    }

    @Test
    void repeatOnce() {
        var res = function.call(interpreter, "hello", 1);
        assertEquals("hello", res);
    }

    @Test
    void repeatZero() {
        var res = function.call(interpreter, "hello", 0);
        assertEquals("", res);
    }

    @Test
    void repeatSingleChar() {
        var res = function.call(interpreter, "x", 5);
        assertEquals("xxxxx", res);
    }

    @Test
    void repeatEmptyString() {
        var res = function.call(interpreter, "", 5);
        assertEquals("", res);
    }

    @Test
    void repeatLargeCount() {
        var res = function.call(interpreter, "a", 100);
        assertEquals("a".repeat(100), res);
    }

    @Test
    void repeatNegativeCount() {
        var res = function.call(interpreter, "hello", -1);
        assertEquals("", res);
    }

    @Test
    void repeatTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello"));
    }

    @Test
    void repeatInvalidFirstArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123, 3));
    }

    @Test
    void repeatInvalidSecondArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", "not-a-number"));
    }
}
