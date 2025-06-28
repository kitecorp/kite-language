package io.zmeu.Runtime.Functions.Numeric;

import io.zmeu.Runtime.BaseRuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
class MaxFunctionTest extends BaseRuntimeTest {
    private final MaxFunction function = new MaxFunction();

    @Test
    void maxInt() {
        var res = function.call(interpreter, 1, 3, 2);
        assertEquals(3, res);
    }

    @Test
    void maxFloat() {
        var res = function.call(interpreter, 1.0f, 3.0f, 2.0f);
        assertEquals(3.0f, res);
    }

    @Test
    void maxDouble() {
        var res = function.call(interpreter, 1.0d, 3.0d, 2.0d);
        assertEquals(3.0d, res);
    }

    @Test
    void maxStr() {
        Assertions.assertThrows(RuntimeException.class, () -> function.call(interpreter, "1", "3", "2"));
    }
}