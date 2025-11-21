package io.kite.stdlib.functions.collections;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlattenFunctionTest extends RuntimeTest {

    private final FlattenFunction function = new FlattenFunction();

    @Test
    void flattenNested() {
        var input = List.of(List.of(1, 2), List.of(3, 4), List.of(5));
        var res = (List<?>) function.call(interpreter, List.of(input));
        assertEquals(List.of(1, 2, 3, 4, 5), res);
    }

    @Test
    void flattenMixed() {
        var input = List.of(1, List.of(2, 3), 4, List.of(5));
        var res = (List<?>) function.call(interpreter, List.of(input));
        assertEquals(List.of(1, 2, 3, 4, 5), res);
    }

    @Test
    void flattenAlreadyFlat() {
        var input = List.of(1, 2, 3, 4);
        var res = (List<?>) function.call(interpreter, List.of(input));
        assertEquals(List.of(1, 2, 3, 4), res);
    }

    @Test
    void flattenEmpty() {
        var res = (List<?>) function.call(interpreter, List.of(List.of()));
        assertEquals(List.of(), res);
    }

    @Test
    void flattenEmptyNested() {
        var input = List.of(List.of(), List.of(), List.of());
        var res = (List<?>) function.call(interpreter, List.of(input));
        assertEquals(List.of(), res);
    }

    @Test
    void flattenOneLevelOnly() {
        var input = List.of(List.of(1, List.of(2, 3)), List.of(4));
        var res = (List<?>) function.call(interpreter, List.of(input));
        assertEquals(List.of(1, List.of(2, 3), 4), res);
    }

    @Test
    void flattenTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of()));
    }

    @Test
    void flattenInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of("not-a-list")));
    }
}
