package cloud.kitelang.stdlib.functions.collections;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReverseFunctionTest extends RuntimeTest {

    private final ReverseFunction function = new ReverseFunction();

    @Test
    void reverseString() {
        var res = function.call(interpreter, "hello");
        assertEquals("olleh", res);
    }

    @Test
    void reverseArray() {
        var res = (List<?>) function.call(interpreter, List.of(List.of(1, 2, 3)));
        assertEquals(List.of(3, 2, 1), res);
    }

    @Test
    void reverseEmptyString() {
        var res = function.call(interpreter, "");
        assertEquals("", res);
    }

    @Test
    void reverseEmptyArray() {
        var res = (List<?>) function.call(interpreter, List.of(List.of()));
        assertEquals(List.of(), res);
    }

    @Test
    void reverseSingleElement() {
        var res = (List<?>) function.call(interpreter, List.of(List.of("a")));
        assertEquals(List.of("a"), res);
    }

    @Test
    void reverseInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }
}
