package cloud.kitelang.stdlib.functions.objects;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HasKeyFunctionTest extends RuntimeTest {

    private final HasKeyFunction function = new HasKeyFunction();

    @Test
    void hasKeyTrue() {
        var obj = Map.of("name", "Alice", "age", 30);
        var res = function.call(interpreter, obj, "name");
        assertEquals(true, res);
    }

    @Test
    void hasKeyFalse() {
        var obj = Map.of("name", "Alice");
        var res = function.call(interpreter, obj, "age");
        assertEquals(false, res);
    }

    @Test
    void hasKeyEmpty() {
        var res = function.call(interpreter, Map.of(), "anyKey");
        assertEquals(false, res);
    }

    @Test
    void hasKeyMultipleKeys() {
        var obj = Map.of("a", 1, "b", 2, "c", 3);
        var res1 = function.call(interpreter, obj, "a");
        var res2 = function.call(interpreter, obj, "b");
        var res3 = function.call(interpreter, obj, "z");
        assertEquals(true, res1);
        assertEquals(true, res2);
        assertEquals(false, res3);
    }

    @Test
    void hasKeyNumericKey() {
        var obj = Map.of(1, "value");
        var res = function.call(interpreter, obj, 1);
        assertEquals(true, res);
    }

    @Test
    void hasKeyTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, Map.of()));
    }

    @Test
    void hasKeyInvalidFirstArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-an-object", "key"));
    }
}
