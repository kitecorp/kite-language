package io.kite.stdlib.functions.string;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MatchesFunctionTest extends RuntimeTest {

    private final MatchesFunction function = new MatchesFunction();

    @Test
    void matchesSimplePattern() {
        var res = function.call(interpreter, "hello", "h.*o");
        assertEquals(true, res);
    }

    @Test
    void matchesNoMatch() {
        var res = function.call(interpreter, "hello", "^world$");
        assertEquals(false, res);
    }

    @Test
    void matchesDigits() {
        var res = function.call(interpreter, "12345", "\\d+");
        assertEquals(true, res);
    }

    @Test
    void matchesEmail() {
        var res = function.call(interpreter, "test@example.com", "^[\\w.-]+@[\\w.-]+\\.\\w+$");
        assertEquals(true, res);
    }

    @Test
    void matchesInvalidEmail() {
        var res = function.call(interpreter, "not-an-email", "^[\\w.-]+@[\\w.-]+\\.\\w+$");
        assertEquals(false, res);
    }

    @Test
    void matchesEmptyString() {
        var res = function.call(interpreter, "", ".*");
        assertEquals(true, res);
    }

    @Test
    void matchesEmptyStringNoMatch() {
        var res = function.call(interpreter, "", ".+");
        assertEquals(false, res);
    }

    @Test
    void matchesPartialMatch() {
        var res = function.call(interpreter, "hello world", ".*world.*");
        assertEquals(true, res);
    }

    @Test
    void matchesTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello"));
    }

    @Test
    void matchesInvalidFirstArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123, "\\d+"));
    }

    @Test
    void matchesInvalidSecondArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", 123));
    }
}
