package io.kite.stdlib.functions.string;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SplitFunctionTest extends RuntimeTest {

    private final SplitFunction function = new SplitFunction();

    @Test
    void splitBySpace() {
        var res = (List<?>) function.call(interpreter, "hello world", " ");
        assertEquals(List.of("hello", "world"), res);
    }

    @Test
    void splitByComma() {
        var res = (List<?>) function.call(interpreter, "a,b,c,d", ",");
        assertEquals(List.of("a", "b", "c", "d"), res);
    }

    @Test
    void splitEmpty() {
        var res = (List<?>) function.call(interpreter, "", ",");
        assertEquals(List.of(""), res);
    }

    @Test
    void splitNoDelimiter() {
        var res = (List<?>) function.call(interpreter, "hello", ",");
        assertEquals(List.of("hello"), res);
    }

    @Test
    void splitInvalidArgCount() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello"));
    }
}
