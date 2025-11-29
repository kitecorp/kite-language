package cloud.kitelang.stdlib.functions.utility;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Base64DecodeFunctionTest extends RuntimeTest {

    private final Base64DecodeFunction function = new Base64DecodeFunction();

    @Test
    void base64DecodeBasic() {
        var res = function.call(interpreter, "aGVsbG8=");
        assertEquals("hello", res);
    }

    @Test
    void base64DecodeEmpty() {
        var res = function.call(interpreter, "");
        assertEquals("", res);
    }

    @Test
    void base64DecodeWithSpaces() {
        var res = function.call(interpreter, "aGVsbG8gd29ybGQ=");
        assertEquals("hello world", res);
    }

    @Test
    void base64DecodeRoundTrip() {
        var encoder = new Base64EncodeFunction();
        var original = "test message";
        var encoded = (String) encoder.call(interpreter, original);
        var decoded = function.call(interpreter, encoded);
        assertEquals(original, decoded);
    }

    @Test
    void base64DecodeSpecialChars() {
        var encoder = new Base64EncodeFunction();
        var original = "test@123!";
        var encoded = (String) encoder.call(interpreter, original);
        var decoded = function.call(interpreter, encoded);
        assertEquals(original, decoded);
    }

    @Test
    void base64DecodeTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }

    @Test
    void base64DecodeInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }

    @Test
    void base64DecodeInvalidString() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-base64!@#"));
    }
}
