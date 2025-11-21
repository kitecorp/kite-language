package io.kite.stdlib.functions.types;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IsNumberFunctionTest extends RuntimeTest {

    private final IsNumberFunction function = new IsNumberFunction();

    @Test
    void isNumberInteger() {
        var res = function.call(interpreter, 42);
        assertEquals(true, res);
    }

    @Test
    void isNumberDouble() {
        var res = function.call(interpreter, 3.14);
        assertEquals(true, res);
    }

    @Test
    void isNumberZero() {
        var res = function.call(interpreter, 0);
        assertEquals(true, res);
    }

    @Test
    void isNumberNegative() {
        var res = function.call(interpreter, -100);
        assertEquals(true, res);
    }

    @Test
    void isNumberString() {
        var res = function.call(interpreter, "123");
        assertEquals(false, res);
    }

    @Test
    void isNumberBoolean() {
        var res = function.call(interpreter, true);
        assertEquals(false, res);
    }

    @Test
    void isNumberArray() {
        var res = function.call((Object) List.of(1, 2, 3));
        assertEquals(false, res);
    }

    @Test
    void isNumberNull() {
        var res = function.call(interpreter, Arrays.asList((Object) null));
        assertEquals(false, res);
    }

    @Test
    void isNumberTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }
}
