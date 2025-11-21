package io.kite.stdlib.functions.collections;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PopFunctionTest extends RuntimeTest {

    private final PopFunction function = new PopFunction();

    @Test
    void popFromArray() {
        var res = (List<?>) function.call(interpreter, List.of(List.of(1, 2, 3)));
        assertEquals(List.of(1, 2), res);
    }

    @Test
    void popFromArrayStrings() {
        var res = (List<?>) function.call(interpreter, List.of(List.of("a", "b", "c")));
        assertEquals(List.of("a", "b"), res);
    }

    @Test
    void popFromSingleElement() {
        var res = (List<?>) function.call(interpreter, List.of(List.of("only")));
        assertEquals(List.of(), res);
    }

    @Test
    void popDoesNotModifyOriginal() {
        var original = List.of(1, 2, 3);
        var res = (List<?>) function.call(interpreter, List.of(original));
        assertEquals(List.of(1, 2), res);
        assertEquals(3, original.size());
    }

    @Test
    void popEmptyArray() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(List.of())));
    }

    @Test
    void popTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(1, 2), "extra"));
    }

    @Test
    void popInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-array"));
    }
}
