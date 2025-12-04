package cloud.kitelang.stdlib.functions.string;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PadEndFunctionTest extends RuntimeTest {

    private final PadEndFunction function = new PadEndFunction();

    @Test
    void padEndBasic() {
        var res = function.call(interpreter, "5", 3);
        assertEquals("5  ", res);
    }

    @Test
    void padEndWithChar() {
        var res = function.call(interpreter, "5", 3, "0");
        assertEquals("500", res);
    }

    @Test
    void padEndAlreadyLong() {
        var res = function.call(interpreter, "hello", 3);
        assertEquals("hello", res);
    }

    @Test
    void padEndExactLength() {
        var res = function.call(interpreter, "abc", 3);
        assertEquals("abc", res);
    }

    @Test
    void padEndCustomChar() {
        var res = function.call(interpreter, "world", 10, "*");
        assertEquals("world*****", res);
    }

    @Test
    void padEndZeroLength() {
        var res = function.call(interpreter, "hello", 0);
        assertEquals("hello", res);
    }

    @Test
    void padEndEmptyString() {
        var res = function.call(interpreter, "", 5);
        assertEquals("     ", res);
    }

    @Test
    void padEndEmptyStringWithChar() {
        var res = function.call(interpreter, "", 5, "x");
        assertEquals("xxxxx", res);
    }

    @Test
    void padEndTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello"));
    }

    @Test
    void padEndInvalidFirstArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123, 5));
    }

    @Test
    void padEndInvalidSecondArg() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "hello", "not-a-number"));
    }
}
