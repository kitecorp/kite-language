package io.kite.stdlib.functions.collections;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortFunctionTest extends RuntimeTest {

    private final SortFunction function = new SortFunction();

    @Test
    void sortNumbers() {
        var res = (List<?>) function.call(interpreter, List.of(List.of(3, 1, 2)));
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void sortStrings() {
        var res = (List<?>) function.call(interpreter, List.of(List.of("c", "a", "b")));
        assertEquals(List.of("a", "b", "c"), res);
    }

    @Test
    void sortAlreadySorted() {
        var res = (List<?>) function.call(interpreter, List.of(List.of(1, 2, 3)));
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void sortReversed() {
        var res = (List<?>) function.call(interpreter, List.of(List.of(3, 2, 1)));
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void sortSingleElement() {
        var res = (List<?>) function.call(interpreter, List.of(List.of(42)));
        assertEquals(List.of(42), res);
    }

    @Test
    void sortEmpty() {
        var res = (List<?>) function.call(interpreter, List.of(List.of()));
        assertEquals(List.of(), res);
    }

    @Test
    void sortDoesNotModifyOriginal() {
        var original = List.of(3, 1, 2);
        var res = (List<?>) function.call(interpreter, List.of(original));
        assertEquals(List.of(1, 2, 3), res);
        assertEquals(3, original.get(0));
    }

    @Test
    void sortWithDuplicates() {
        var res = (List<?>) function.call(interpreter, List.of(List.of(3, 1, 2, 1, 3)));
        assertEquals(List.of(1, 1, 2, 3, 3), res);
    }

    @Test
    void sortNegativeNumbers() {
        var res = (List<?>) function.call(interpreter, List.of(List.of(-1, 3, -5, 0, 2)));
        assertEquals(List.of(-5, -1, 0, 2, 3), res);
    }

    @Test
    void sortTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(1, 2), "extra"));
    }

    @Test
    void sortInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-array"));
    }

    @Test
    void sortMixedTypesThrows() {
        assertThrows(RuntimeException.class, () ->
                function.call(interpreter, List.of(List.of(1, "string", 2))));
    }
}
