package io.kite.stdlib.functions.numeric;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomFunctionTest extends RuntimeTest {

    private final RandomFunction function = new RandomFunction();

    @Test
    void randomReturnsDouble() {
        var res = function.call(interpreter);
        assertTrue(res instanceof Double);
    }

    @Test
    void randomInRange() {
        var res = (Double) function.call(interpreter);
        assertTrue(res >= 0.0 && res < 1.0);
    }

    @Test
    void randomDifferentValues() {
        var res1 = (Double) function.call(interpreter);
        var res2 = (Double) function.call(interpreter);
        // Statistically very unlikely to be equal
        assertNotEquals(res1, res2);
    }

    @Test
    void randomMultipleCalls() {
        for (int i = 0; i < 100; i++) {
            var res = (Double) function.call(interpreter);
            assertTrue(res >= 0.0 && res < 1.0);
        }
    }
}
