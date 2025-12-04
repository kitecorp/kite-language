package cloud.kitelang.stdlib.functions.string;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ToLowerCaseFunctionTest extends RuntimeTest {

    private final ToLowerCaseFunction function = new ToLowerCaseFunction();

    @Test
    void toLowerCaseUppercase() {
        var res = function.call(interpreter, "HELLO");
        assertEquals("hello", res);
    }

    @Test
    void toLowerCaseMixed() {
        var res = function.call(interpreter, "HeLLo WoRLd");
        assertEquals("hello world", res);
    }

    @Test
    void toLowerCaseAlreadyLower() {
        var res = function.call(interpreter, "hello");
        assertEquals("hello", res);
    }

    @Test
    void toLowerCaseEmpty() {
        var res = function.call(interpreter, "");
        assertEquals("", res);
    }

    @Test
    void toLowerCaseWithNumbers() {
        var res = function.call(interpreter, "Hello123");
        assertEquals("hello123", res);
    }

    @Test
    void toLowerCaseWithSpecialChars() {
        var res = function.call(interpreter, "HELLO-WORLD!");
        assertEquals("hello-world!", res);
    }

    @Test
    void toLowerCaseTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", "world"));
    }

    @Test
    void toLowerCaseInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }
}
