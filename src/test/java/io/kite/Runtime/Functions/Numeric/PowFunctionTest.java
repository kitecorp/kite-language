package io.kite.Runtime.Functions.Numeric;

import io.kite.Base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
class PowFunctionTest extends RuntimeTest {
    private final PowFunction function = new PowFunction();

    @Test
    void ceil() {
        var res = function.call(interpreter, 3, 2);
        assertEquals(9, res);
    }

    @Test
    void ceilMaxArgs() {
        Assertions.assertThrows(RuntimeException.class, () -> function.call(interpreter, 2.0d, 3.0d, 1.0d));
    }

}