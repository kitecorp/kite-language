package cloud.kitelang.stdlib.functions.utility;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base64EncodeFunctionTest extends RuntimeTest {

    private final Base64EncodeFunction function = new Base64EncodeFunction();

    @Test
    void base64EncodeBasic() {
        var res = function.call(interpreter, "hello");
        assertEquals("aGVsbG8=", res);
    }

    @Test
    void base64EncodeEmpty() {
        var res = function.call(interpreter, "");
        assertEquals("", res);
    }

    @Test
    void base64EncodeWithSpaces() {
        var res = function.call(interpreter, "hello world");
        assertEquals("aGVsbG8gd29ybGQ=", res);
    }

    @Test
    void base64EncodeSpecialChars() {
        var res = function.call(interpreter, "test@123!");
        assertNotNull(res);
        assertTrue(res.toString().matches("^[A-Za-z0-9+/=]*$"));
    }

    @Test
    void base64EncodeUnicode() {
        var res = function.call(interpreter, "こんにちは");
        assertNotNull(res);
        assertTrue(res.toString().matches("^[A-Za-z0-9+/=]*$"));
    }

    @Test
    void base64EncodeLongString() {
        var input = "This is a longer string that will be encoded to base64";
        var res = function.call(interpreter, input);
        assertNotNull(res);
        assertTrue(res.toString().length() > input.length());
    }

    @Test
    void base64EncodeTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }

    @Test
    void base64EncodeInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }
}
