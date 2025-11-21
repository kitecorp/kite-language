package io.kite.stdlib.functions.collections;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistinctFunctionTest extends RuntimeTest {

    private final DistinctFunction function = new DistinctFunction();

    @Test
    void distinctWithDuplicates() {
        var res = (List<?>) function.call(interpreter, List.of(List.of(1, 2, 2, 3, 1)));
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void distinctNoDuplicates() {
        var res = (List<?>) function.call(interpreter, List.of(List.of(1, 2, 3)));
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void distinctStrings() {
        var res = (List<?>) function.call(interpreter, List.of(List.of("a", "b", "a", "c")));
        assertEquals(List.of("a", "b", "c"), res);
    }

    @Test
    void distinctEmpty() {
        var res = (List<?>) function.call(interpreter, List.of(List.of()));
        assertEquals(List.of(), res);
    }
}
