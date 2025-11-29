package cloud.kitelang.stdlib.functions.string;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EndsWithFunctionTest extends RuntimeTest {

    private final EndsWithFunction function = new EndsWithFunction();

    @Test
    void endsWithTrue() {
        var res = function.call(interpreter, "hello world", "world");
        assertEquals(true, res);
    }

    @Test
    void endsWithFalse() {
        var res = function.call(interpreter, "hello world", "hello");
        assertEquals(false, res);
    }

    @Test
    void endsWithEmpty() {
        var res = function.call(interpreter, "hello", "");
        assertEquals(true, res);
    }

    @Test
    void endsWithSameString() {
        var res = function.call(interpreter, "hello", "hello");
        assertEquals(true, res);
    }

    @Test
    void endsWithLongerSuffix() {
        var res = function.call(interpreter, "world", "hello world");
        assertEquals(false, res);
    }

    @Test
    void endsWithCaseSensitive() {
        var res = function.call(interpreter, "Hello", "HELLO");
        assertEquals(false, res);
    }

    @Test
    void endsWithSingleChar() {
        var res = function.call(interpreter, "hello", "o");
        assertEquals(true, res);
    }

    @Test
    void endsWithEmptyString() {
        var res = function.call(interpreter, "", "hello");
        assertEquals(false, res);
    }

    @Test
    void endsWithFileExtension() {
        var res = function.call(interpreter, "document.pdf", ".pdf");
        assertEquals(true, res);
    }

    @Test
    void endsWithTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello"));
    }

    @Test
    void endsWithTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "a", "b", "c"));
    }

    @Test
    void endsWithInvalidFirstArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123, "a"));
    }

    @Test
    void endsWithInvalidSecondArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", 123));
    }
}
