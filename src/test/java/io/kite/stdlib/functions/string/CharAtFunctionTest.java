package io.kite.stdlib.functions.string;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CharAtFunctionTest extends RuntimeTest {

    private final CharAtFunction function = new CharAtFunction();

    @Test
    void charAtFirst() {
        var res = function.call(interpreter, "hello", 0);
        assertEquals("h", res);
    }

    @Test
    void charAtMiddle() {
        var res = function.call(interpreter, "hello", 2);
        assertEquals("l", res);
    }

    @Test
    void charAtLast() {
        var res = function.call(interpreter, "hello", 4);
        assertEquals("o", res);
    }

    @Test
    void charAtSingleChar() {
        var res = function.call(interpreter, "x", 0);
        assertEquals("x", res);
    }

    @Test
    void charAtOutOfBounds() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", 10));
    }

    @Test
    void charAtNegativeIndex() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", -1));
    }

    @Test
    void charAtEmptyString() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "", 0));
    }

    @Test
    void charAtTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello"));
    }

    @Test
    void charAtInvalidFirstArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123, 0));
    }

    @Test
    void charAtInvalidSecondArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", "not-a-number"));
    }
}
