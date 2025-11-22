package io.kite.stdlib.functions.objects;

import io.kite.base.RuntimeTest;
import io.kite.execution.values.NullValue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("Function 'get' removed due to namespace conflict with common resource names")
class GetFunctionTest extends RuntimeTest {

    private final GetFunction function = new GetFunction();

    @Test
    void getExistingKey() {
        var obj = Map.of("name", "Alice", "age", 30);
        var res = function.call(interpreter, obj, "name");
        assertEquals("Alice", res);
    }

    @Test
    void getNonExistingKeyNoDefault() {
        var obj = Map.of("name", "Alice");
        var res = function.call(interpreter, obj, "age");
        assertTrue(res instanceof NullValue);
    }

    @Test
    void getNonExistingKeyWithDefault() {
        var obj = Map.of("name", "Alice");
        var res = function.call(interpreter, obj, "age", 25);
        assertEquals(25, res);
    }

    @Test
    void getWithDefaultUsed() {
        var obj = Map.of("a", 1);
        var res = function.call(interpreter, obj, "b", "default");
        assertEquals("default", res);
    }

    @Test
    void getWithDefaultNotUsed() {
        var obj = Map.of("a", 1);
        var res = function.call(interpreter, obj, "a", 999);
        assertEquals(1, res);
    }

    @Test
    void getNumericValue() {
        var obj = Map.of("count", 42);
        var res = function.call(interpreter, obj, "count");
        assertEquals(42, res);
    }

    @Test
    void getFromEmptyObject() {
        var res = function.call(interpreter, Map.of(), "key", "default");
        assertEquals("default", res);
    }

    @Test
    void getTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, Map.of()));
    }

    @Test
    void getInvalidFirstArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-an-object", "key"));
    }
}
