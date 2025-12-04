package cloud.kitelang.stdlib.functions.collections;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ZipFunctionTest extends RuntimeTest {

    private final ZipFunction function = new ZipFunction();

    @Test
    void zipEqualLength() {
        var res = (List<?>) function.call(interpreter, List.of(1, 2, 3), List.of("a", "b", "c"));
        assertEquals(List.of(List.of(1, "a"), List.of(2, "b"), List.of(3, "c")), res);
    }

    @Test
    void zipFirstShorter() {
        var res = (List<?>) function.call(interpreter, List.of(1, 2), List.of("a", "b", "c"));
        assertEquals(List.of(List.of(1, "a"), List.of(2, "b")), res);
    }

    @Test
    void zipSecondShorter() {
        var res = (List<?>) function.call(interpreter, List.of(1, 2, 3), List.of("a", "b"));
        assertEquals(List.of(List.of(1, "a"), List.of(2, "b")), res);
    }

    @Test
    void zipOneEmpty() {
        var res = (List<?>) function.call(interpreter, List.of(), List.of(1, 2, 3));
        assertEquals(List.of(), res);
    }

    @Test
    void zipBothEmpty() {
        var res = (List<?>) function.call(interpreter, List.of(), List.of());
        assertEquals(List.of(), res);
    }

    @Test
    void zipSingleElements() {
        var res = (List<?>) function.call(interpreter, List.of(1), List.of("a"));
        assertEquals(List.of(List.of(1, "a")), res);
    }

    @Test
    void zipTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(1, 2, 3)));
    }

    @Test
    void zipFirstInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-a-list", List.of(1, 2)));
    }

    @Test
    void zipSecondInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(1, 2), "not-a-list"));
    }
}
