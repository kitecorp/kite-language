package io.zmeu.Runtime.Functions.Numeric;

import io.zmeu.Base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
class AbsFunctionTest extends RuntimeTest {
    private final AbsFunction function = new AbsFunction();

    @Test
    void absPositive() {
        var res = function.call(interpreter, 2.1);
        assertEquals(2.1, res);
    }

    @Test
    void absZero() {
        var res = function.call(interpreter, 0);
        assertEquals(0, res);
    }

    @Test
    void absZeroKeepImplicit() {
        var res = function.call(interpreter, 0.0);
        assertEquals(0.0, res);
    }

    @Test
    void absNegative() {
        var res = function.call(interpreter, -2.3);
        assertEquals(2.3, res);
    }

    @Test
    void ceilMaxArgs() {
        Assertions.assertThrows(RuntimeException.class, () -> function.call(interpreter, 2.0d, 3.0d, 1.0d));
    }

}