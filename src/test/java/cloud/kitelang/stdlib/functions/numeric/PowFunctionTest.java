package cloud.kitelang.stdlib.functions.numeric;

import cloud.kitelang.base.RuntimeTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
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