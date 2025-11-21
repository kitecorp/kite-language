package io.kite.stdlib.functions.string;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StartsWithFunctionTest extends RuntimeTest {

    private final StartsWithFunction function = new StartsWithFunction();

    @Test
    void startsWithTrue() {
        var res = function.call(interpreter, "hello world", "hello");
        assertEquals(true, res);
    }

    @Test
    void startsWithFalse() {
        var res = function.call(interpreter, "hello world", "world");
        assertEquals(false, res);
    }

    @Test
    void startsWithEmpty() {
        var res = function.call(interpreter, "hello", "");
        assertEquals(true, res);
    }

    @Test
    void startsWithSameString() {
        var res = function.call(interpreter, "hello", "hello");
        assertEquals(true, res);
    }

    @Test
    void startsWithLongerPrefix() {
        var res = function.call(interpreter, "hello", "hello world");
        assertEquals(false, res);
    }

    @Test
    void startsWithCaseSensitive() {
        var res = function.call(interpreter, "Hello", "hello");
        assertEquals(false, res);
    }

    @Test
    void startsWithSingleChar() {
        var res = function.call(interpreter, "hello", "h");
        assertEquals(true, res);
    }

    @Test
    void startsWithEmptyString() {
        var res = function.call(interpreter, "", "hello");
        assertEquals(false, res);
    }

    @Test
    void startsWithTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello"));
    }

    @Test
    void startsWithTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "a", "b", "c"));
    }

    @Test
    void startsWithInvalidFirstArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123, "a"));
    }

    @Test
    void startsWithInvalidSecondArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", 123));
    }
}
