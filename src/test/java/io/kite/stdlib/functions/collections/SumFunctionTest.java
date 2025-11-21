package io.kite.stdlib.functions.collections;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SumFunctionTest extends RuntimeTest {

    private final SumFunction function = new SumFunction();

    @Test
    void sumIntegers() {
        var res = (Double) function.call(interpreter, List.of(List.of(1, 2, 3, 4, 5)));
        assertEquals(15.0, res);
    }

    @Test
    void sumDecimals() {
        var res = (Double) function.call(interpreter, List.of(List.of(1.5, 2.5, 3.0)));
        assertEquals(7.0, res, 0.001);
    }

    @Test
    void sumMixed() {
        var res = (Double) function.call(interpreter, List.of(List.of(1, 2.5, 3)));
        assertEquals(6.5, res, 0.001);
    }

    @Test
    void sumWithNegatives() {
        var res = (Double) function.call(interpreter, List.of(List.of(10, -5, 3, -2)));
        assertEquals(6.0, res);
    }

    @Test
    void sumEmpty() {
        var res = (Double) function.call(interpreter, List.of(List.of()));
        assertEquals(0.0, res);
    }

    @Test
    void sumSingleElement() {
        var res = (Double) function.call(interpreter, List.of(List.of(42)));
        assertEquals(42.0, res);
    }

    @Test
    void sumZeros() {
        var res = (Double) function.call(interpreter, List.of(List.of(0, 0, 0)));
        assertEquals(0.0, res);
    }

    @Test
    void sumTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of()));
    }

    @Test
    void sumInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of("not-a-list")));
    }

    @Test
    void sumNonNumericElements() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(List.of(1, "string", 3))));
    }
}
