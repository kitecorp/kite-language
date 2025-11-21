package io.kite.stdlib.functions.string;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ToUpperCaseFunctionTest extends RuntimeTest {

    private final ToUpperCaseFunction function = new ToUpperCaseFunction();

    @Test
    void toUpperCaseLowercase() {
        var res = function.call(interpreter, "hello");
        assertEquals("HELLO", res);
    }

    @Test
    void toUpperCaseMixed() {
        var res = function.call(interpreter, "HeLLo WoRLd");
        assertEquals("HELLO WORLD", res);
    }

    @Test
    void toUpperCaseAlreadyUpper() {
        var res = function.call(interpreter, "HELLO");
        assertEquals("HELLO", res);
    }

    @Test
    void toUpperCaseEmpty() {
        var res = function.call(interpreter, "");
        assertEquals("", res);
    }

    @Test
    void toUpperCaseInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }
}
