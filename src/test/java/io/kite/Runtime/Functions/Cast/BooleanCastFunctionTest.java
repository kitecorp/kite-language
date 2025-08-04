package io.kite.Runtime.Functions.Cast;

import io.kite.Base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BooleanCastFunctionTest extends RuntimeTest {
    private final BooleanCastFunction function = new BooleanCastFunction();

    @Test
    void trueToTrue() {
        var res = function.call(interpreter, "true");
        assertEquals(true, res);
    }

    @Test
    void yesToTrue() {
        var res = function.call(interpreter, "yes");
        assertEquals(true, res);
    }

    @Test
    void falseToFalse() {
        var res = function.call(interpreter, "false");
        assertEquals(false, res);
    }

    @Test
    void noToFalse() {
        var res = function.call(interpreter, "no");
        assertEquals(false, res);
    }


}