package cloud.kitelang.stdlib.functions.utility;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UuidFunctionTest extends RuntimeTest {

    private final UuidFunction function = new UuidFunction();

    @Test
    void uuidFormat() {
        var res = (String) function.call(interpreter);
        // UUID format: 8-4-4-4-12 characters
        assertTrue(res.matches("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$"));
    }

    @Test
    void uuidValidUUID() {
        var res = (String) function.call(interpreter);
        // Should be parseable as UUID
        assertDoesNotThrow(() -> UUID.fromString(res));
    }

    @Test
    void uuidUnique() {
        var uuids = new HashSet<String>();
        for (int i = 0; i < 100; i++) {
            var uuid = (String) function.call(interpreter);
            uuids.add(uuid);
        }
        // All 100 UUIDs should be unique
        assertEquals(100, uuids.size());
    }

    @Test
    void uuidNotEmpty() {
        var res = (String) function.call(interpreter);
        assertFalse(res.isEmpty());
    }

    @Test
    void uuidLength() {
        var res = (String) function.call(interpreter);
        // UUID with dashes is 36 characters
        assertEquals(36, res.length());
    }
}
