package cloud.kitelang.stdlib.functions.numeric;

import cloud.kitelang.base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
class CeilFunctionTest extends RuntimeTest {
    private final CeilFunction function = new CeilFunction();

    @Test
    void ceil() {
        var res = function.call(interpreter, 2.1);
        assertEquals(3, res);
    }

    @Test
    void ceilMaxArgs() {
        Assertions.assertThrows(RuntimeException.class, () -> function.call(interpreter, 2.0d, 3.0d, 1.0d));
    }

}