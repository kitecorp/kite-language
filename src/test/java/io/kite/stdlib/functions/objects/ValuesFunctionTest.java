package io.kite.stdlib.functions.objects;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ValuesFunctionTest extends RuntimeTest {

    private final ValuesFunction function = new ValuesFunction();

    @Test
    void valuesBasic() {
        var obj = Map.of("name", "Alice", "age", 30);
        var res = (List<?>) function.call(interpreter, obj);
        assertTrue(res.containsAll(List.of("Alice", 30)));
        assertEquals(2, res.size());
    }

    @Test
    void valuesEmpty() {
        var res = (List<?>) function.call(interpreter, Map.of());
        assertEquals(List.of(), res);
    }

    @Test
    void valuesSingleValue() {
        var obj = Map.of("key", "value");
        var res = (List<?>) function.call(interpreter, obj);
        assertEquals(List.of("value"), res);
    }

    @Test
    void valuesMultipleValues() {
        var obj = Map.of("a", 1, "b", 2, "c", 3);
        var res = (List<?>) function.call(interpreter, obj);
        assertEquals(3, res.size());
        assertTrue(res.containsAll(List.of(1, 2, 3)));
    }

    @Test
    void valuesDuplicateValues() {
        var obj = Map.of("a", "same", "b", "same", "c", "different");
        var res = (List<?>) function.call(interpreter, obj);
        assertEquals(3, res.size());
    }

    @Test
    void valuesTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }

    @Test
    void valuesInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-an-object"));
    }
}
