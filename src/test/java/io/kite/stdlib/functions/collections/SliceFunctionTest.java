package io.kite.stdlib.functions.collections;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SliceFunctionTest extends RuntimeTest {

    private final SliceFunction function = new SliceFunction();

    @Test
    void sliceMiddle() {
        var input = new ArrayList<>(List.of(0, 1, 2, 3, 4));
        var res = (List<?>) function.call(interpreter, List.of(input, 1, 3));
        assertEquals(List.of(1, 2), res);
    }

    @Test
    void sliceFromStart() {
        var input = new ArrayList<>(List.of(0, 1, 2, 3, 4));
        var res = (List<?>) function.call(interpreter, List.of(input, 0, 2));
        assertEquals(List.of(0, 1), res);
    }

    @Test
    void sliceToEnd() {
        var input = new ArrayList<>(List.of(0, 1, 2, 3, 4));
        var res = (List<?>) function.call(interpreter, List.of(input, 2));
        assertEquals(List.of(2, 3, 4), res);
    }

    @Test
    void sliceEmpty() {
        var input = new ArrayList<>(List.of(0, 1, 2));
        var res = (List<?>) function.call(interpreter, List.of(input, 2, 2));
        assertEquals(List.of(), res);
    }

    @Test
    void sliceInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-array", 0, 2));
    }
}
