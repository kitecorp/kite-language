package io.kite.stdlib.functions.types;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IsArrayFunctionTest extends RuntimeTest {

    private final IsArrayFunction function = new IsArrayFunction();

    @Test
    void isArrayTrue() {
        var res = function.call((Object) List.of(1, 2, 3));
        assertEquals(true, res);
    }

    @Test
    void isArrayEmpty() {
        var res = function.call((Object) List.of());
        assertEquals(true, res);
    }

    @Test
    void isArrayStrings() {
        var res = function.call((Object) List.of("a", "b", "c"));
        assertEquals(true, res);
    }

    @Test
    void isArrayNested() {
        var res = function.call((Object) List.of(List.of(1, 2), List.of(3, 4)));
        assertEquals(true, res);
    }

    @Test
    void isArrayNumber() {
        var res = function.call(123);
        assertEquals(false, res);
    }

    @Test
    void isArrayString() {
        var res = function.call("array");
        assertEquals(false, res);
    }

    @Test
    void isArrayBoolean() {
        var res = function.call(true);
        assertEquals(false, res);
    }

    @Test
    void isArrayNull() {
        var res = function.call(interpreter, Arrays.asList((Object) null));
        assertEquals(false, res);
    }

    @Test
    void isArrayTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }
}
