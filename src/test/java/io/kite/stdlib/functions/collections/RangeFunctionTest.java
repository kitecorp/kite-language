package io.kite.stdlib.functions.collections;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RangeFunctionTest extends RuntimeTest {

    private final RangeFunction function = new RangeFunction();

    @Test
    void rangeOneArg() {
        var res = (List<?>) function.call(interpreter, 5);
        assertEquals(List.of(0, 1, 2, 3, 4), res);
    }

    @Test
    void rangeTwoArgs() {
        var res = (List<?>) function.call(interpreter, 2, 5);
        assertEquals(List.of(2, 3, 4), res);
    }

    @Test
    void rangeThreeArgs() {
        var res = (List<?>) function.call(interpreter, 0, 10, 2);
        assertEquals(List.of(0, 2, 4, 6, 8), res);
    }

    @Test
    void rangeNegativeStep() {
        var res = (List<?>) function.call(interpreter, 5, 0, -1);
        assertEquals(List.of(5, 4, 3, 2, 1), res);
    }

    @Test
    void rangeZeroStep() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 0, 5, 0));
    }
}
