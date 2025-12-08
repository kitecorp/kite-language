package cloud.kitelang.semantics.decorators;

import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.semantics.TypeError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("@minLength @maxLength")
public class MinMaxLengthTest extends CheckerTest {

    @Test
    void decoratorMaxLengthMissingNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @maxLength
                input string something"""));
    }

    @Test
    void decoratorMaxLength() {
        eval("""
                @maxLength(10)
                input string something""");
    }

    @Test
    void decoratorMaxLengthNumber() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @maxLength(10)
                        input number something""")
        );
    }

    @Test
    void decoratorMaxLengthArray() {
        eval("""
                @maxLength(10)
                input string[] something""");
    }

    @Test
    void decoratorMaxLengthArrayNumber() {
        eval("""
                @maxLength(10)
                input number[] something""");
    }

    @Test
    void decoratorMaxLengthArrayAny() {
        eval("""
                @maxLength(10)
                input any[] something""");
    }

    @Test
    void decoratorMaxLengthArrayObject() {
        eval("""
                @maxLength(10)
                input object[] something""");
    }

    @Test
    void decoratorMaxLengthSchema() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @maxLength
                schema something{}"""));
    }

    @Test
    void decoratorMaxLengthNegative() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @maxLength(-10)
                input string something"""));
    }

    @Test
    void decoratorMinLengthMissingNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @minLength
                input string something"""));
    }

    @Test
    void decoratorMinLength() {
        eval("""
                @minLength(10)
                input string something""");
    }

    @Test
    void decoratorMinLengthNumber() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @minLength(10)
                        input number something""")
        );
    }

    @Test
    void decoratorMinLengthArray() {
        eval("""
                @minLength(10)
                input string[] something""");
    }

    @Test
    void decoratorMinLengthArrayNumber() {
        eval("""
                @minLength(10)
                input number[] something""");
    }

    @Test
    void decoratorMinLengthArrayAny() {
        eval("""
                @minLength(10)
                input any[] something""");
    }

    @Test
    void decoratorMinLengthArrayObject() {
        eval("""
                @minLength(10)
                input object[] something""");
    }

    @Test
    void decoratorMinLengthSchema() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @minLength
                schema something{}"""));
    }

    @Test
    void decoratorMinLengthNegative() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @minLength(-10)
                input string something"""));
    }


}
