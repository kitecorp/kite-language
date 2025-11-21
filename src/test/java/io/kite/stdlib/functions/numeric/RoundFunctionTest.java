package io.kite.stdlib.functions.numeric;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RoundFunctionTest extends RuntimeTest {

    private final RoundFunction function = new RoundFunction();

    @Test
    void roundPositiveUp() {
        var res = function.call(interpreter, 2.6);
        assertEquals(3L, res);
    }

    @Test
    void roundPositiveDown() {
        var res = function.call(interpreter, 2.4);
        assertEquals(2L, res);
    }

    @Test
    void roundNegativeUp() {
        var res = function.call(interpreter, -2.4);
        assertEquals(-2L, res);
    }

    @Test
    void roundNegativeDown() {
        var res = function.call(interpreter, -2.6);
        assertEquals(-3L, res);
    }

    @Test
    void roundHalfUp() {
        var res = function.call(interpreter, 2.5);
        assertEquals(3L, res);
    }

    @Test
    void roundInteger() {
        var res = function.call(interpreter, 5);
        assertEquals(5, res);
    }

    @Test
    void roundZero() {
        var res = function.call(interpreter, 0.0);
        assertEquals(0L, res);
    }

    @Test
    void roundTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 2.5, 3.0));
    }

    @Test
    void roundInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not a number"));
    }
}
