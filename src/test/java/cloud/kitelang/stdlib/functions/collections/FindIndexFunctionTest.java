package cloud.kitelang.stdlib.functions.collections;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FindIndexFunctionTest extends RuntimeTest {

    private final FindIndexFunction function = new FindIndexFunction();

    @Test
    void findIndexExisting() {
        var res = function.call(interpreter, List.of(1, 2, 3, 4), 3);
        assertEquals(2, res);
    }

    @Test
    void findIndexNonExisting() {
        var res = function.call(interpreter, List.of(1, 2, 3), 10);
        assertEquals(-1, res);
    }

    @Test
    void findIndexFirstElement() {
        var res = function.call(interpreter, List.of("a", "b", "c"), "a");
        assertEquals(0, res);
    }

    @Test
    void findIndexLastElement() {
        var res = function.call(interpreter, List.of(10, 20, 30), 30);
        assertEquals(2, res);
    }

    @Test
    void findIndexDuplicate() {
        var res = function.call(interpreter, List.of(1, 2, 2, 3), 2);
        assertEquals(1, res);
    }

    @Test
    void findIndexEmpty() {
        var res = function.call(interpreter, List.of(), 1);
        assertEquals(-1, res);
    }

    @Test
    void findIndexTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, List.of(1, 2, 3)));
    }

    @Test
    void findIndexInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, "not-a-list", 1));
    }
}
