package io.kite.stdlib.functions.collections;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Disabled("Function 'first' removed due to namespace conflict with common resource names")
class FirstFunctionTest extends RuntimeTest {

    private final FirstFunction function = new FirstFunction();

    @Test
    void firstOfString() {
        var res = function.call(interpreter, "hello");
        assertEquals("h", res);
    }

    @Test
    void firstOfArray() {
        var res = function.call(interpreter, List.of(List.of(1, 2, 3)));
        assertEquals(1, res);
    }

    @Test
    void firstOfArrayStrings() {
        var res = function.call(interpreter, List.of(List.of("a", "b", "c")));
        assertEquals("a", res);
    }

    @Test
    void firstOfSingleElement() {
        var res = function.call(interpreter, List.of(List.of("only")));
        assertEquals("only", res);
    }

    @Test
    void firstOfSingleChar() {
        var res = function.call(interpreter, "x");
        assertEquals("x", res);
    }

    @Test
    void firstEmptyString() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, ""));
    }

    @Test
    void firstEmptyArray() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(List.of())));
    }

    @Test
    void firstTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", "world"));
    }

    @Test
    void firstInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }
}
