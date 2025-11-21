package io.kite.stdlib.functions.collections;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LastFunctionTest extends RuntimeTest {

    private final LastFunction function = new LastFunction();

    @Test
    void lastOfString() {
        var res = function.call(interpreter, "hello");
        assertEquals("o", res);
    }

    @Test
    void lastOfArray() {
        var res = function.call(interpreter, List.of(List.of(1, 2, 3)));
        assertEquals(3, res);
    }

    @Test
    void lastOfArrayStrings() {
        var res = function.call(interpreter, List.of(List.of("a", "b", "c")));
        assertEquals("c", res);
    }

    @Test
    void lastOfSingleElement() {
        var res = function.call(interpreter, List.of(List.of("only")));
        assertEquals("only", res);
    }

    @Test
    void lastOfSingleChar() {
        var res = function.call(interpreter, "x");
        assertEquals("x", res);
    }

    @Test
    void lastEmptyString() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, ""));
    }

    @Test
    void lastEmptyArray() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(List.of())));
    }

    @Test
    void lastTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", "world"));
    }

    @Test
    void lastInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }
}
