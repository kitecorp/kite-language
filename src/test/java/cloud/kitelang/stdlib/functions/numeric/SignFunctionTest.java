package cloud.kitelang.stdlib.functions.numeric;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SignFunctionTest extends RuntimeTest {

    private final SignFunction function = new SignFunction();

    @Test
    void signPositive() {
        var res = function.call(interpreter, 5);
        assertEquals(1, res);
    }

    @Test
    void signNegative() {
        var res = function.call(interpreter, -5);
        assertEquals(-1, res);
    }

    @Test
    void signZero() {
        var res = function.call(interpreter, 0);
        assertEquals(0, res);
    }

    @Test
    void signPositiveDecimal() {
        var res = function.call(interpreter, 2.5);
        assertEquals(1, res);
    }

    @Test
    void signNegativeDecimal() {
        var res = function.call(interpreter, -2.5);
        assertEquals(-1, res);
    }

    @Test
    void signVerySmallPositive() {
        var res = function.call(interpreter, 0.0001);
        assertEquals(1, res);
    }

    @Test
    void signVerySmallNegative() {
        var res = function.call(interpreter, -0.0001);
        assertEquals(-1, res);
    }

    @Test
    void signTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 5, 10));
    }

    @Test
    void signInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not a number"));
    }
}
