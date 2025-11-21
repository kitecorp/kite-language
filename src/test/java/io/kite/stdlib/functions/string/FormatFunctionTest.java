package io.kite.stdlib.functions.string;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FormatFunctionTest extends RuntimeTest {

    private final FormatFunction function = new FormatFunction();

    @Test
    void formatSingleArg() {
        var res = function.call(interpreter, List.of("Hello {0}", "world"));
        assertEquals("Hello world", res);
    }

    @Test
    void formatMultipleArgs() {
        var res = function.call(interpreter, List.of("My name is {0} and I am {1} years old", "Alice", 30));
        assertEquals("My name is Alice and I am 30 years old", res);
    }

    @Test
    void formatNumbers() {
        var res = function.call(interpreter, List.of("Result: {0} + {1} = {2}", 5, 10, 15));
        assertEquals("Result: 5 + 10 = 15", res);
    }

    @Test
    void formatRepeatedPlaceholder() {
        var res = function.call(interpreter, List.of("{0} and {0} again", "test"));
        assertEquals("test and test again", res);
    }

    @Test
    void formatNoPlaceholders() {
        var res = function.call(interpreter, List.of("Hello world"));
        assertEquals("Hello world", res);
    }

    @Test
    void formatEmptyString() {
        var res = function.call(interpreter, List.of(""));
        assertEquals("", res);
    }

    @Test
    void formatMixedTypes() {
        var res = function.call(interpreter, List.of("String: {0}, Number: {1}, Boolean: {2}", "hello", 42, true));
        assertEquals("String: hello, Number: 42, Boolean: true", res);
    }

    @Test
    void formatTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of()));
    }

    @Test
    void formatInvalidFirstArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(123, "arg")));
    }
}
