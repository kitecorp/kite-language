package io.kite.stdlib.functions.numeric;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClampFunctionTest extends RuntimeTest {

    private final ClampFunction function = new ClampFunction();

    @Test
    void clampWithinRange() {
        var res = function.call(interpreter, 5, 0, 10);
        assertEquals(5.0, res);
    }

    @Test
    void clampBelowMin() {
        var res = function.call(interpreter, -5, 0, 10);
        assertEquals(0.0, res);
    }

    @Test
    void clampAboveMax() {
        var res = function.call(interpreter, 15, 0, 10);
        assertEquals(10.0, res);
    }

    @Test
    void clampAtMin() {
        var res = function.call(interpreter, 0, 0, 10);
        assertEquals(0.0, res);
    }

    @Test
    void clampAtMax() {
        var res = function.call(interpreter, 10, 0, 10);
        assertEquals(10.0, res);
    }

    @Test
    void clampNegativeRange() {
        var res = function.call(interpreter, -15, -10, -5);
        assertEquals(-10.0, res);
    }

    @Test
    void clampDecimal() {
        var res = function.call(interpreter, 7.5, 5.0, 10.0);
        assertEquals(7.5, res);
    }

    @Test
    void clampTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 5, 0));
    }

    @Test
    void clampInvalidFirstArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not a number", 0, 10));
    }

    @Test
    void clampInvalidSecondArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 5, "not a number", 10));
    }

    @Test
    void clampInvalidThirdArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 5, 0, "not a number"));
    }
}
