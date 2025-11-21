package io.kite.stdlib.functions.types;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IsStringFunctionTest extends RuntimeTest {

    private final IsStringFunction function = new IsStringFunction();

    @Test
    void isStringTrue() {
        var res = function.call(interpreter, "hello");
        assertEquals(true, res);
    }

    @Test
    void isStringEmptyString() {
        var res = function.call(interpreter, "");
        assertEquals(true, res);
    }

    @Test
    void isStringNumber() {
        var res = function.call(interpreter, 123);
        assertEquals(false, res);
    }

    @Test
    void isStringBoolean() {
        var res = function.call(interpreter, true);
        assertEquals(false, res);
    }

    @Test
    void isStringArray() {
        var res = function.call((Object) List.of(1, 2, 3));
        assertEquals(false, res);
    }

    @Test
    void isStringNull() {
        var res = function.call(interpreter, Arrays.asList((Object) null));
        assertEquals(false, res);
    }

    @Test
    void isStringTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }
}
