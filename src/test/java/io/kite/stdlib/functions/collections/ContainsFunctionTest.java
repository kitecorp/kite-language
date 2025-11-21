package io.kite.stdlib.functions.collections;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContainsFunctionTest extends RuntimeTest {

    private final ContainsFunction function = new ContainsFunction();

    @Test
    void containsStringTrue() {
        var res = function.call(interpreter, "hello world", "world");
        assertEquals(true, res);
    }

    @Test
    void containsStringFalse() {
        var res = function.call(interpreter, "hello world", "foo");
        assertEquals(false, res);
    }

    @Test
    void containsArrayTrue() {
        var res = function.call(interpreter, List.of(1, 2, 3), 2);
        assertEquals(true, res);
    }

    @Test
    void containsArrayFalse() {
        var res = function.call(interpreter, List.of(1, 2, 3), 5);
        assertEquals(false, res);
    }

    @Test
    void containsEmptyString() {
        var res = function.call(interpreter, "hello", "");
        assertEquals(true, res);
    }

    @Test
    void containsInvalidArgCount() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello"));
    }
}
