package io.kite.stdlib.functions.collections;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IsEmptyFunctionTest extends RuntimeTest {

    private final IsEmptyFunction function = new IsEmptyFunction();

    @Test
    void isEmptyStringTrue() {
        var res = function.call(interpreter, "");
        assertEquals(true, res);
    }

    @Test
    void isEmptyStringFalse() {
        var res = function.call(interpreter, "hello");
        assertEquals(false, res);
    }

    @Test
    void isEmptyArrayTrue() {
        var res = function.call(interpreter, List.of(List.of()));
        assertEquals(true, res);
    }

    @Test
    void isEmptyArrayFalse() {
        var res = function.call(interpreter, List.of(List.of(1, 2, 3)));
        assertEquals(false, res);
    }

    @Test
    void isEmptyArraySingleElement() {
        var res = function.call(interpreter, List.of(List.of("a")));
        assertEquals(false, res);
    }

    @Test
    void isEmptyWhitespace() {
        var res = function.call(interpreter, "   ");
        assertEquals(false, res);
    }

    @Test
    void isEmptyTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", "world"));
    }

    @Test
    void isEmptyInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }
}
