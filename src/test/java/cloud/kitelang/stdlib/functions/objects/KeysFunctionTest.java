package cloud.kitelang.stdlib.functions.objects;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KeysFunctionTest extends RuntimeTest {

    private final KeysFunction function = new KeysFunction();

    @Test
    void keysBasic() {
        var obj = Map.of("name", "Alice", "age", 30);
        var res = (List<?>) function.call(interpreter, obj);
        assertTrue(res.containsAll(List.of("name", "age")));
        assertEquals(2, res.size());
    }

    @Test
    void keysEmpty() {
        var res = (List<?>) function.call(interpreter, Map.of());
        assertEquals(List.of(), res);
    }

    @Test
    void keysSingleKey() {
        var obj = Map.of("only", "value");
        var res = (List<?>) function.call(interpreter, obj);
        assertEquals(List.of("only"), res);
    }

    @Test
    void keysMultipleKeys() {
        var obj = Map.of("a", 1, "b", 2, "c", 3, "d", 4);
        var res = (List<?>) function.call(interpreter, obj);
        assertEquals(4, res.size());
        assertTrue(res.containsAll(List.of("a", "b", "c", "d")));
    }

    @Test
    void keysTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }

    @Test
    void keysInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-an-object"));
    }
}
