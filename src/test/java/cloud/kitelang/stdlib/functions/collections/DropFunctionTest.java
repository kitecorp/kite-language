package cloud.kitelang.stdlib.functions.collections;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DropFunctionTest extends RuntimeTest {

    private final DropFunction function = new DropFunction();

    @Test
    void dropMiddle() {
        var res = (List<?>) function.call(interpreter, List.of(1, 2, 3, 4, 5), 2);
        assertEquals(List.of(3, 4, 5), res);
    }

    @Test
    void dropAll() {
        var res = (List<?>) function.call(interpreter, List.of(1, 2, 3), 5);
        assertEquals(List.of(), res);
    }

    @Test
    void dropNone() {
        var res = (List<?>) function.call(interpreter, List.of(1, 2, 3), 0);
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void dropOne() {
        var res = (List<?>) function.call(interpreter, List.of("a", "b", "c"), 1);
        assertEquals(List.of("b", "c"), res);
    }

    @Test
    void dropFromEmpty() {
        var res = (List<?>) function.call(interpreter, List.of(), 5);
        assertEquals(List.of(), res);
    }

    @Test
    void dropNegative() {
        // DropFunction throws IndexOutOfBoundsException for negative count because subList(-1, ...) is invalid
        assertThrows(IndexOutOfBoundsException.class, () ->
                function.call(interpreter, List.of(1, 2, 3), -1)
        );
    }

    @Test
    void dropTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(1, 2, 3)));
    }

    @Test
    void dropInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-a-list", 2));
    }
}
