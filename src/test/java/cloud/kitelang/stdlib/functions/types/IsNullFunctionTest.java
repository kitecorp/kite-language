package cloud.kitelang.stdlib.functions.types;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.NullValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IsNullFunctionTest extends RuntimeTest {

    private final IsNullFunction function = new IsNullFunction();

    @Test
    void isNullTrue() {
        var res = function.call(interpreter, Arrays.asList((Object) null));
        assertEquals(true, res);
    }

    @Test
    void isNullNullValue() {
        var res = function.call(interpreter, new NullValue());
        assertEquals(true, res);
    }

    @Test
    void isNullZero() {
        var res = function.call(interpreter, 0);
        assertEquals(false, res);
    }

    @Test
    void isNullEmptyString() {
        var res = function.call(interpreter, "");
        assertEquals(false, res);
    }

    @Test
    void isNullFalse() {
        var res = function.call(interpreter, false);
        assertEquals(false, res);
    }

    @Test
    void isNullEmptyArray() {
        var res = function.call((Object) List.of());
        assertEquals(false, res);
    }

    @Test
    void isNullString() {
        var res = function.call(interpreter, "null");
        assertEquals(false, res);
    }

    @Test
    void isNullTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }
}
