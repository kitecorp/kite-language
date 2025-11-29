package cloud.kitelang.stdlib.functions.datetime;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimestampFunctionTest extends RuntimeTest {

    private final TimestampFunction function = new TimestampFunction();

    @Test
    void timestampReturnsLong() {
        var res = function.call(interpreter);
        assertNotNull(res);
        assertTrue(res instanceof Long);
    }

    @Test
    void timestampReturnsPositive() {
        var res = (Long) function.call(interpreter);
        assertTrue(res > 0);
    }

    @Test
    void timestampProgresses() throws InterruptedException {
        var res1 = (Long) function.call(interpreter);
        Thread.sleep(10);
        var res2 = (Long) function.call(interpreter);
        assertTrue(res2 > res1);
    }
}
