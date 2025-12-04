package cloud.kitelang.stdlib.functions.collections;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PushFunctionTest extends RuntimeTest {

    private final PushFunction function = new PushFunction();

    @Test
    void pushToArray() {
        var res = (List<?>) function.call(interpreter, List.of(1, 2), 3);
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void pushToEmptyArray() {
        var res = (List<?>) function.call(interpreter, List.of(), "hello");
        assertEquals(List.of("hello"), res);
    }

    @Test
    void pushString() {
        var res = (List<?>) function.call(interpreter, List.of("a", "b"), "c");
        assertEquals(List.of("a", "b", "c"), res);
    }

    @Test
    void pushInvalidArgCount() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(1, 2)));
    }

    @Test
    void pushInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-array", "element"));
    }
}
