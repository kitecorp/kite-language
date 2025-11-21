package io.kite.execution.decorators;

import io.kite.semantics.TypeError;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * All the tests from print but with added sensitive values.
 */
@Log4j2
public class MinMaxValueTests extends DecoratorTests {

    @Test
    void outputMinValue() {
        eval("""
                @minValue(10)
                output number something = 10
                """);
    }

    @Test
    void outputMinValueGreaterThan() {
        eval("""
                @minValue(10)
                output number something = 11
                """);
    }

    @Test
    void outputMinValueLessThan() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @minValue(10)
                output number something = 9
                """));
    }

    @Test
    void outputMinValueWrongInit() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @minValue(10)
                output number something = "hello"
                """));
    }

    @Test
    void outputMinValueNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @minValue(10)
                output number something = -9
                """));
    }

    @Test
    void outputMaxValue() {
        eval("""
                @maxValue(10)
                output number something = 10
                """);
    }

    @Test
    void outputMaxValueGreaterThan() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @maxValue(10)
                output number something = 11
                """));
    }

    @Test
    void outputMaxValueLessThan() {
        eval("""
                @maxValue(10)
                output number something = 9
                """);
    }

    @Test
    void outputMaxValueWrongInit() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @maxValue(10)
                output number something = "hello"
                """));
    }

    @Test
    void outputMaxValueNegative() {
        eval("""
                @maxValue(10)
                output number something = -9
                """);
    }

    @Test
    void outputMinMaxValue() {
        eval("""
                @maxValue(10)
                @minValue(0)
                output number something = 5
                """);
    }

    @Test
    void outputMinMaxValueNegative() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> eval("""
                @maxValue(10)
                @minValue(6)
                output number something = 5
                """));
    }


}
