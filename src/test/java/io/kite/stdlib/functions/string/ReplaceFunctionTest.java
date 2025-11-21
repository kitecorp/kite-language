package io.kite.stdlib.functions.string;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReplaceFunctionTest extends RuntimeTest {

    private final ReplaceFunction function = new ReplaceFunction();

    @Test
    void replaceSingleOccurrence() {
        var res = function.call(interpreter, "hello world", "world", "universe");
        assertEquals("hello universe", res);
    }

    @Test
    void replaceMultipleOccurrences() {
        var res = function.call(interpreter, "foo bar foo", "foo", "baz");
        assertEquals("baz bar baz", res);
    }

    @Test
    void replaceNotFound() {
        var res = function.call(interpreter, "hello world", "xyz", "abc");
        assertEquals("hello world", res);
    }

    @Test
    void replaceWithEmpty() {
        var res = function.call(interpreter, "hello world", "world", "");
        assertEquals("hello ", res);
    }

    @Test
    void replaceEmpty() {
        var res = function.call(interpreter, "", "hello", "world");
        assertEquals("", res);
    }

    @Test
    void replaceEntireString() {
        var res = function.call(interpreter, "hello", "hello", "goodbye");
        assertEquals("goodbye", res);
    }

    @Test
    void replaceWithLongerString() {
        var res = function.call(interpreter, "a", "a", "hello");
        assertEquals("hello", res);
    }

    @Test
    void replaceCaseSensitive() {
        var res = function.call(interpreter, "Hello World", "hello", "hi");
        assertEquals("Hello World", res);
    }

    @Test
    void replaceTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", "world"));
    }

    @Test
    void replaceTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "a", "b", "c", "d"));
    }

    @Test
    void replaceInvalidFirstArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123, "a", "b"));
    }

    @Test
    void replaceInvalidSecondArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", 123, "b"));
    }

    @Test
    void replaceInvalidThirdArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", "a", 123));
    }
}
