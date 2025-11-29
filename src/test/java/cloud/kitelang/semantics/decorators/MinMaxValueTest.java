package cloud.kitelang.semantics.Decorators;

import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.semantics.TypeError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("@miValue @maxValue")
public class MinMaxValueTest extends CheckerTest {


    @Test
    void decoratorMinValueMissingNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @minValue
                input string something"""));
    }

    @Test
    void decoratorMinValue() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @minValue(10)
                input string something"""));
    }

    @Test
    void decoratorMinValueNumber() {
        eval("""
                @minValue(10)
                input number something""");
    }

    @Test
    void decoratorMinValueArray() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @minValue(10)
                input string[] something"""));
    }

    @Test
    void decoratorMinValueArrayNumber() {
        eval("""
                @minValue(10)
                input number[] something""");
    }

    @Test
    void decoratorMinValueArrayAny() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @minValue(10)
                input any[] something"""));
    }

    @Test
    void decoratorMinValueArrayObject() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @minValue(10)
                input object[] something"""));
    }

    @Test
    void decoratorMinValueSchema() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @minValue
                schema something{}"""));
    }

    @Test
    void decoratorMinValueNegative() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @minValue(-10)
                input string something"""));
    }

    @Test
    void decoratorMaxValueMissingNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @maxValue
                input string something"""));
    }

    @Test
    void decoratorMaxValue() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @maxValue(10)
                input string something"""));
    }

    @Test
    void decoratorMaxValueNumber() {
        eval("""
                @maxValue(10)
                input number something""");
    }

    @Test
    void decoratorMaxValueArray() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @maxValue(10)
                input string[] something"""));
    }

    @Test
    void decoratorMaxValueArrayNumber() {
        eval("""
                @maxValue(10)
                input number[] something""");
    }

    @Test
    void decoratorMaxValueArrayAny() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @maxValue(10)
                input any[] something"""));
    }

    @Test
    void decoratorMaxValueArrayObject() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @maxValue(10)
                input object[] something"""));
    }

    @Test
    void decoratorMaxValueSchema() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @maxValue
                schema something{}"""));
    }

    @Test
    void decoratorMaxValueNegative() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @maxValue(-10)
                input string something"""));
    }

}
