package cloud.kitelang.stdlib.functions.types;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ToNumberFunctionTest extends RuntimeTest {

    private final ToNumberFunction function = new ToNumberFunction();

    @Test
    void toNumberFromString() {
        var res = (Integer) function.call(interpreter, "123");
        assertEquals(123, res);
    }

    @Test
    void toNumberFromStringDecimal() {
        var res = (Double) function.call(interpreter, "3.14");
        assertEquals(3.14, res, 0.001);
    }

    @Test
    void toNumberFromStringNegative() {
        var res = (Integer) function.call(interpreter, "-42");
        assertEquals(-42, res);
    }

    @Test
    void toNumberFromNumber() {
        var res = function.call(interpreter, 42);
        assertEquals(42, res);
    }

    @Test
    void toNumberFromBooleanTrue() {
        var res = (Integer) function.call(interpreter, true);
        assertEquals(1, res);
    }

    @Test
    void toNumberFromBooleanFalse() {
        var res = (Integer) function.call(interpreter, false);
        assertEquals(0, res);
    }

    @Test
    void toNumberInvalidString() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-a-number"));
    }

    @Test
    void toNumberEmptyString() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, ""));
    }

    @Test
    void toNumberNull() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, (Object) null));
    }

    @Test
    void toNumberTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }
}
