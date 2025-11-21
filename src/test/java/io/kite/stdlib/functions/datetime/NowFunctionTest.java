package io.kite.stdlib.functions.datetime;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NowFunctionTest extends RuntimeTest {

    private final NowFunction function = new NowFunction();

    @Test
    void nowReturnsString() {
        var res = function.call(interpreter);
        assertNotNull(res);
        assertTrue(res instanceof String);
    }

    @Test
    void nowContainsDate() {
        var res = (String) function.call(interpreter);
        assertTrue(res.contains("-"));
        assertTrue(res.contains(":"));
    }
}
