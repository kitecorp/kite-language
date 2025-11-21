package io.kite.stdlib.functions.numeric;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ModFunctionTest extends RuntimeTest {

    private final ModFunction function = new ModFunction();

    @Test
    void modBasic() {
        var res = function.call(interpreter, 10, 3);
        assertEquals(1.0, res);
    }

    @Test
    void modEvenDivision() {
        var res = function.call(interpreter, 10, 5);
        assertEquals(0.0, res);
    }

    @Test
    void modNegativeDividend() {
        var res = function.call(interpreter, -10, 3);
        assertEquals(-1.0, res);
    }

    @Test
    void modNegativeDivisor() {
        var res = function.call(interpreter, 10, -3);
        assertEquals(1.0, res);
    }

    @Test
    void modDecimal() {
        var res = (Double) function.call(interpreter, 10.5, 3.0);
        assertEquals(1.5, res, 0.001);
    }

    @Test
    void modZeroDividend() {
        var res = function.call(interpreter, 0, 5);
        assertEquals(0.0, res);
    }

    @Test
    void modLargerDivisor() {
        var res = function.call(interpreter, 5, 10);
        assertEquals(5.0, res);
    }

    @Test
    void modTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 10));
    }

    @Test
    void modInvalidFirstArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not a number", 3));
    }

    @Test
    void modInvalidSecondArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 10, "not a number"));
    }
}
