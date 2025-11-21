package io.kite.stdlib.functions.types;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IsObjectFunctionTest extends RuntimeTest {

    private final IsObjectFunction function = new IsObjectFunction();

    @Test
    void isObjectTrue() {
        var res = function.call(interpreter, Map.of("key", "value"));
        assertEquals(true, res);
    }

    @Test
    void isObjectEmpty() {
        var res = function.call(interpreter, Map.of());
        assertEquals(true, res);
    }

    @Test
    void isObjectMultipleKeys() {
        var res = function.call(interpreter, Map.of("name", "Alice", "age", 30));
        assertEquals(true, res);
    }

    @Test
    void isObjectNumber() {
        var res = function.call(interpreter, 123);
        assertEquals(false, res);
    }

    @Test
    void isObjectString() {
        var res = function.call(interpreter, "object");
        assertEquals(false, res);
    }

    @Test
    void isObjectBoolean() {
        var res = function.call(interpreter, true);
        assertEquals(false, res);
    }

    @Test
    void isObjectArray() {
        var res = function.call((Object) List.of(1, 2, 3));
        assertEquals(false, res);
    }

    @Test
    void isObjectNull() {
        var res = function.call(interpreter, Arrays.asList((Object) null));
        assertEquals(false, res);
    }

    @Test
    void isObjectTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }
}
