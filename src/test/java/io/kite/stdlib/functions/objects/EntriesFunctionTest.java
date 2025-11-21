package io.kite.stdlib.functions.objects;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntriesFunctionTest extends RuntimeTest {

    private final EntriesFunction function = new EntriesFunction();

    @Test
    void entriesBasic() {
        var obj = Map.of("name", "Alice");
        var res = (List<?>) function.call(interpreter, obj);
        assertEquals(1, res.size());
        var entry = (List<?>) res.get(0);
        assertEquals("name", entry.get(0));
        assertEquals("Alice", entry.get(1));
    }

    @Test
    void entriesEmpty() {
        var res = (List<?>) function.call(interpreter, Map.of());
        assertEquals(List.of(), res);
    }

    @Test
    void entriesMultiple() {
        var obj = Map.of("a", 1, "b", 2);
        var res = (List<?>) function.call(interpreter, obj);
        assertEquals(2, res.size());

        // Each entry is a [key, value] pair
        for (Object item : res) {
            var entry = (List<?>) item;
            assertEquals(2, entry.size());
        }
    }

    @Test
    void entriesMixedTypes() {
        var obj = Map.of("string", "value", "number", 42, "boolean", true);
        var res = (List<?>) function.call(interpreter, obj);
        assertEquals(3, res.size());
    }

    @Test
    void entriesTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }

    @Test
    void entriesInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-an-object"));
    }
}
