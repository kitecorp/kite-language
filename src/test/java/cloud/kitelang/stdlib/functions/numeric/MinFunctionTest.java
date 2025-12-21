package cloud.kitelang.stdlib.functions.numeric;

import cloud.kitelang.base.RuntimeTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class MinFunctionTest extends RuntimeTest {
    private final MinFunction function = new MinFunction();

    @Test
    void maxInt() {
        var res = function.call(interpreter, 2, 3, 1);
        assertEquals(1, res);
    }

    @Test
    void maxFloat() {
        var res = function.call(interpreter, 2.0f, 3.0f, 1.0f);
        assertEquals(1.0f, res);
    }

    @Test
    void maxDouble() {
        var res = function.call(interpreter, 2.0d, 3.0d, 1.0d);
        assertEquals(1.0d, res);
    }

    @Test
    void maxStr() {
        Assertions.assertThrows(RuntimeException.class, () -> function.call(interpreter, "1", "3", "2"));
    }

}