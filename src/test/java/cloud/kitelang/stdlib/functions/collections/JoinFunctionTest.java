package cloud.kitelang.stdlib.functions.collections;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JoinFunctionTest extends RuntimeTest {

    private final JoinFunction function = new JoinFunction();

    @Test
    void joinWithComma() {
        var res = function.call(interpreter, List.of("a", "b", "c"), ", ");
        assertEquals("a, b, c", res);
    }

    @Test
    void joinWithoutDelimiter() {
        var res = function.call(interpreter, List.of(List.of("a", "b", "c")));
        assertEquals("abc", res);
    }

    @Test
    void joinNumbers() {
        var res = function.call(interpreter, List.of(1, 2, 3), "-");
        assertEquals("1-2-3", res);
    }

    @Test
    void joinEmpty() {
        var res = function.call(interpreter, List.of(), ",");
        assertEquals("", res);
    }

    @Test
    void joinSingleElement() {
        var res = function.call(interpreter, List.of("hello"), ",");
        assertEquals("hello", res);
    }

    @Test
    void joinInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-array", ","));
    }
}
