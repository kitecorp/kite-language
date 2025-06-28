package io.zmeu.Runtime.Functions.Numeric;

import io.zmeu.Runtime.BaseRuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
class FloorFunctionTest extends BaseRuntimeTest {
    private final FloorFunction function = new FloorFunction();

    @Test
    void floor() {
        var res = function.call(interpreter, 2.1);
        assertEquals(2, res);
    }

    @Test
    void floorMaxArgs() {
        Assertions.assertThrows(RuntimeException.class, () -> function.call(interpreter, 2.0d, 3.0d, 1.0d));
    }

}