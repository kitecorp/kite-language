package cloud.kitelang.stdlib.functions.collections;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AverageFunctionTest extends RuntimeTest {

    private final AverageFunction function = new AverageFunction();

    @Test
    void averageIntegers() {
        var res = (Double) function.call(interpreter, List.of(List.of(1, 2, 3, 4, 5)));
        assertEquals(3.0, res);
    }

    @Test
    void averageDecimals() {
        var res = (Double) function.call(interpreter, List.of(List.of(1.5, 2.5, 3.0)));
        assertEquals(2.333, res, 0.001);
    }

    @Test
    void averageMixed() {
        var res = (Double) function.call(interpreter, List.of(List.of(10, 20, 30)));
        assertEquals(20.0, res);
    }

    @Test
    void averageWithNegatives() {
        var res = (Double) function.call(interpreter, List.of(List.of(-10, 0, 10)));
        assertEquals(0.0, res);
    }

    @Test
    void averageSingleElement() {
        var res = (Double) function.call(interpreter, List.of(List.of(42)));
        assertEquals(42.0, res);
    }

    @Test
    void averageZeros() {
        var res = (Double) function.call(interpreter, List.of(List.of(0, 0, 0)));
        assertEquals(0.0, res);
    }

    @Test
    void averageEmpty() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(List.of())));
    }

    @Test
    void averageTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of()));
    }

    @Test
    void averageInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of("not-a-list")));
    }

    @Test
    void averageNonNumericElements() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(List.of(1, "string", 3))));
    }
}
