package cloud.kitelang.stdlib.functions.numeric;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqrtFunctionTest extends RuntimeTest {

    private final SqrtFunction function = new SqrtFunction();

    @Test
    void sqrtPerfectSquare() {
        var res = function.call(interpreter, 9);
        assertEquals(3.0, res);
    }

    @Test
    void sqrtNonPerfectSquare() {
        var res = (Double) function.call(interpreter, 2);
        assertEquals(1.414, res, 0.001);
    }

    @Test
    void sqrtZero() {
        var res = function.call(interpreter, 0);
        assertEquals(0.0, res);
    }

    @Test
    void sqrtOne() {
        var res = function.call(interpreter, 1);
        assertEquals(1.0, res);
    }

    @Test
    void sqrtDecimal() {
        var res = (Double) function.call(interpreter, 6.25);
        assertEquals(2.5, res);
    }

    @Test
    void sqrtLargeNumber() {
        var res = function.call(interpreter, 100);
        assertEquals(10.0, res);
    }

    @Test
    void sqrtTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 9, 16));
    }

    @Test
    void sqrtInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not a number"));
    }
}
