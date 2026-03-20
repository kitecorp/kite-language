package cloud.kitelang.stdlib.functions.string;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.DeferredFunctionCall;
import cloud.kitelang.execution.values.DeferredValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LengthFunctionTest extends RuntimeTest {

    private final LengthFunction function = new LengthFunction();

    @Test
    void lengthOfString() {
        var res = function.call(interpreter, "hello");
        assertEquals(5, res);
    }

    @Test
    void lengthOfEmptyString() {
        var res = function.call(interpreter, "");
        assertEquals(0, res);
    }

    @Test
    void lengthOfArray() {
        var res = function.call(interpreter, List.of(List.of(1, 2, 3)));
        assertEquals(3, res);
    }

    @Test
    void lengthOfEmptyArray() {
        var res = function.call(interpreter, List.of(List.of()));
        assertEquals(0, res);
    }

    @Test
    void lengthTooManyArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", "world"));
    }

    @Test
    void lengthInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }

    @Test
    @DisplayName("length() propagates DeferredValue as DeferredFunctionCall")
    void lengthOfDeferredValue() {
        var deferred = new DeferredValue("subnet", "arn");
        var result = function.call(interpreter, List.of(deferred));

        assertInstanceOf(DeferredFunctionCall.class, result);
        var deferredCall = (DeferredFunctionCall) result;
        assertEquals("length", deferredCall.functionName());
        assertEquals("subnet", deferredCall.dependencyName());
        assertEquals("arn", deferredCall.propertyPath());
    }
}
