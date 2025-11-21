package io.kite.stdlib.functions.objects;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MergeFunctionTest extends RuntimeTest {

    private final MergeFunction function = new MergeFunction();

    @Test
    void mergeTwoObjects() {
        var obj1 = Map.of("a", 1);
        var obj2 = Map.of("b", 2);
        var res = (Map<?, ?>) function.call(interpreter, obj1, obj2);
        assertEquals(2, res.size());
        assertEquals(1, res.get("a"));
        assertEquals(2, res.get("b"));
    }

    @Test
    void mergeOverwrite() {
        var obj1 = Map.of("a", 1, "b", 2);
        var obj2 = Map.of("b", 3, "c", 4);
        var res = (Map<?, ?>) function.call(interpreter, obj1, obj2);
        assertEquals(3, res.size());
        assertEquals(1, res.get("a"));
        assertEquals(3, res.get("b")); // obj2 overwrites obj1
        assertEquals(4, res.get("c"));
    }

    @Test
    void mergeMultipleObjects() {
        var obj1 = Map.of("a", 1);
        var obj2 = Map.of("b", 2);
        var obj3 = Map.of("c", 3);
        var res = (Map<?, ?>) function.call(interpreter, obj1, obj2, obj3);
        assertEquals(3, res.size());
        assertEquals(1, res.get("a"));
        assertEquals(2, res.get("b"));
        assertEquals(3, res.get("c"));
    }

    @Test
    void mergeEmpty() {
        var obj1 = Map.of("a", 1);
        var obj2 = Map.of();
        var res = (Map<?, ?>) function.call(interpreter, obj1, obj2);
        assertEquals(1, res.size());
        assertEquals(1, res.get("a"));
    }

    @Test
    void mergeAllEmpty() {
        var res = (Map<?, ?>) function.call(interpreter, Map.of(), Map.of());
        assertEquals(0, res.size());
    }

    @Test
    void mergeTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, Map.of()));
    }

    @Test
    void mergeInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-an-object", Map.of()));
    }
}
