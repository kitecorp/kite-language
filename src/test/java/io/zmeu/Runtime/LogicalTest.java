package io.zmeu.Runtime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LogicalTest extends BaseRuntimeTest {

    @Test
    void trueOrTrue() {
        var res = (Boolean) eval("true || true");
        Assertions.assertTrue(res);
    }

    @Test
    void trueOrFalse() {
        // true || false -> true
        var res = (Boolean) eval("true || false");
        Assertions.assertTrue(res);
    }

    @Test
    void falseOrTrue() {
        // false || true -> true
        var res = (Boolean) eval("false || true");
        Assertions.assertTrue(res);
    }

    @Test
    void falseOrFalse() {
        // false || false -> true
        var res = (Boolean) eval("false || false");
        Assertions.assertFalse(res);
    }

    @Test
    void falseAndFalse() {
        // false && false -> false
        var res = (Boolean) eval("false && false");
        Assertions.assertFalse(res);
    }

    @Test
    void trueAndTrue() {
        // true && true -> true
        var res = (Boolean) eval("true && true");
        Assertions.assertTrue(res);
    }

    @Test
    void trueAndFalse() {
        // true && false -> false
        var res = (Boolean) eval("true && false");
        Assertions.assertFalse(res);
    }

    @Test
    void falseAndTrue() {
        // false && true -> false
        var res = (Boolean) eval("false && true");
        Assertions.assertFalse(res);
    }


    @Test
    void nullAndNumber() {
        // null && 2 -> doesn't make any sense so throw exception
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("null && 2"));
    }

    @Test
    void numberAndNull() {
        // 2 && null -> -> doesn't make any sense so throw
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("2 && null"));
    }

    @Test
    void numberAndNumber() {
        // 1 && 2 -> doesn't make any sense so throw
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("1 && 2"));
    }

    @Test
    void trueAndNumber() {
        // 1 && 2 -> doesn't make any sense so throw
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("true && 2 "));
    }

    @Test
    void falseAndNumber() {
        // 1 && 2 -> doesn't make any sense so throw
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("true && 2 "));
    }

    @Test
    void numAndTrue() {
        // 1 && 2 -> doesn't make any sense so throw
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("2 && true "));
    }

    @Test
    void numAndFalse() {
        // 1 && 2 -> doesn't make any sense so throw
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("2 && false "));
    }
    @Test
    void trueOrNumber() {
        // 1 && 2 -> doesn't make any sense so throw
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("true || 2 "));
    }

    @Test
    void falseOrNumber() {
        // 1 && 2 -> doesn't make any sense so throw
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("true || 2 "));
    }

    @Test
    void numOrTrue() {
        // 1 && 2 -> doesn't make any sense so throw
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("2 || true "));
    }

    @Test
    void numOrFalse() {
        // 1 && 2 -> doesn't make any sense so throw
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("2 || false "));
    }

}
