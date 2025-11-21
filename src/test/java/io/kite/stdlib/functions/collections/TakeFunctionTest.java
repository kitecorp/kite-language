package io.kite.stdlib.functions.collections;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TakeFunctionTest extends RuntimeTest {

    private final TakeFunction function = new TakeFunction();

    @Test
    void takeMiddle() {
        var res = (List<?>) function.call(interpreter, List.of(1, 2, 3, 4, 5), 3);
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void takeAll() {
        var res = (List<?>) function.call(interpreter, List.of(1, 2, 3), 5);
        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void takeNone() {
        var res = (List<?>) function.call(interpreter, List.of(1, 2, 3), 0);
        assertEquals(List.of(), res);
    }

    @Test
    void takeOne() {
        var res = (List<?>) function.call(interpreter, List.of("a", "b", "c"), 1);
        assertEquals(List.of("a"), res);
    }

    @Test
    void takeFromEmpty() {
        var res = (List<?>) function.call(interpreter, List.of(), 5);
        assertEquals(List.of(), res);
    }

    @Test
    void takeNegative() {
        // TakeFunction throws IllegalArgumentException for negative count because subList(0, -1) is invalid
        assertThrows(IllegalArgumentException.class, () ->
                function.call(interpreter, List.of(1, 2, 3), -1)
        );
    }

    @Test
    void takeTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(1, 2, 3)));
    }

    @Test
    void takeInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-a-list", 2));
    }
}
