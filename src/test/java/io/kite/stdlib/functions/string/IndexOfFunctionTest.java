package io.kite.stdlib.functions.string;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IndexOfFunctionTest extends RuntimeTest {

    private final IndexOfFunction function = new IndexOfFunction();

    @Test
    void indexOfFound() {
        var res = function.call(interpreter, "hello world", "world");
        assertEquals(6, res);
    }

    @Test
    void indexOfNotFound() {
        var res = function.call(interpreter, "hello world", "xyz");
        assertEquals(-1, res);
    }

    @Test
    void indexOfAtStart() {
        var res = function.call(interpreter, "hello world", "hello");
        assertEquals(0, res);
    }

    @Test
    void indexOfMultipleOccurrences() {
        var res = function.call(interpreter, "foo bar foo", "foo");
        assertEquals(0, res);
    }

    @Test
    void indexOfSingleChar() {
        var res = function.call(interpreter, "hello", "e");
        assertEquals(1, res);
    }

    @Test
    void indexOfEmptyString() {
        var res = function.call(interpreter, "hello", "");
        assertEquals(0, res);
    }

    @Test
    void indexOfInEmptyString() {
        var res = function.call(interpreter, "", "hello");
        assertEquals(-1, res);
    }

    @Test
    void indexOfCaseSensitive() {
        var res = function.call(interpreter, "Hello World", "world");
        assertEquals(-1, res);
    }

    @Test
    void indexOfTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello"));
    }

    @Test
    void indexOfTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "a", "b", "c"));
    }

    @Test
    void indexOfInvalidFirstArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123, "a"));
    }

    @Test
    void indexOfInvalidSecondArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", 123));
    }
}
