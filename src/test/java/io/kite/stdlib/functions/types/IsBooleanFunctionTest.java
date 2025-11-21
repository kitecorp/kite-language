package io.kite.stdlib.functions.types;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IsBooleanFunctionTest extends RuntimeTest {

    private final IsBooleanFunction function = new IsBooleanFunction();

    @Test
    void isBooleanTrue() {
        var res = function.call(interpreter, true);
        assertEquals(true, res);
    }

    @Test
    void isBooleanFalse() {
        var res = function.call(interpreter, false);
        assertEquals(true, res);
    }

    @Test
    void isBooleanNumber() {
        var res = function.call(interpreter, 1);
        assertEquals(false, res);
    }

    @Test
    void isBooleanString() {
        var res = function.call(interpreter, "true");
        assertEquals(false, res);
    }

    @Test
    void isBooleanArray() {
        var res = function.call((Object) List.of(true, false));
        assertEquals(false, res);
    }

    @Test
    void isBooleanNull() {
        var res = function.call(interpreter, Arrays.asList((Object) null));
        assertEquals(false, res);
    }

    @Test
    void isBooleanTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }
}
