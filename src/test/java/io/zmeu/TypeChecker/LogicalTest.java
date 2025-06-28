package io.zmeu.TypeChecker;

import io.zmeu.Base.CheckerTest;
import io.zmeu.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LogicalTest extends CheckerTest {

    @Test
    void trueOrTrue() {
        var res = eval("true || true");
        Assertions.assertEquals(ValueType.Boolean, res);
    }

    @Test
    void trueOrFalse() {
        // true || false -> true
        var res = eval("true || false");
        Assertions.assertEquals(ValueType.Boolean, res);
    }

    @Test
    void falseOrTrue() {
        // false || true -> true
        var res = eval("false || true");
        Assertions.assertEquals(ValueType.Boolean, res);
    }

    @Test
    void falseOrFalse() {
        // false || false -> true
        var res = eval("false || false");
        Assertions.assertEquals(ValueType.Boolean, res);
    }

    @Test
    void nullAndNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("null && 2"));
    }

    @Test
    void nullOrNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("null || 2"));
    }

    @Test
    void booleanOrNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("true || 2"));
    }

    @Test
    void booleanOrString() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                true || "test" 
                """));
    }

    @Test
    void booleanAndNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                true && 3 
                """));
    }

    @Test
    void stringAndString() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                "true" && "3" 
                """));
    }

    @Test
    void nullAndNull() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                null && null 
                """));
    }

    @Test
    void nullOrNull() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                null || null 
                """));
    }

    @Test
    void numberAndBoolean() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                3 && true 
                """));
    }

    @Test
    void booleanAndString() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                true && "test" 
                """));
    }

    @Test
    void stringAndBoolean() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                "test" && true 
                """));
    }


    @Test
    void stringAndFalse() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                "test" && false 
                """));
    }

    @Test
    void nullAndFalse() {
        Assertions.assertThrows(TypeError.class, () -> eval(" null && false "));
    }

    @Test
    void nullOrFalse() {
        Assertions.assertThrows(TypeError.class, () -> eval(" null || false "));
    }

    @Test
    void nullAndTrue() {
        Assertions.assertThrows(TypeError.class, () -> eval(" null && true "));
    }

    @Test
    void nullOrTrue() {
        Assertions.assertThrows(TypeError.class, () -> eval(" null || true "));
    }

    @Test
    void numberAndNull() {
        Assertions.assertThrows(TypeError.class, () -> eval("2 && null"));
    }

    @Test
    void numberAndNumber() {
        // 1 && 2 -> throw
        Assertions.assertThrows(TypeError.class, () -> eval("1 && 2"));
    }

    @Test
    void falseAndFalse() {
        // false && false -> false
        var res = eval("false && false");
        Assertions.assertEquals(ValueType.Boolean, res);
    }

    @Test
    void trueAndFalse() {
        // false && false -> false
        var res = eval("true && false");
        Assertions.assertEquals(ValueType.Boolean, res);
    }

    @Test
    void trueAndTrue() {
        // false && false -> false
        var res = eval("true && true");
        Assertions.assertEquals(ValueType.Boolean, res);
    }

    @Test
    void falseAndTrue() {
        // false && false -> false
        var res = eval("false && true");
        Assertions.assertEquals(ValueType.Boolean, res);
    }

}
