package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.Types.AnyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TypeChecker Annotation")
public class DecoratorTest extends CheckerTest {

    @Test
    void decoratorSensitive() {
        var res = eval("""
                @sensitive
                output any something = null""");

        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void decoratorSensitiveInvalidArgs() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @sensitive(2)
                output any something = null
                """));
    }

    @Test
    void decoratorSensitiveInvalidElement() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @sensitive
                var x = 2"""));
    }

    @Test
    void decoratorUnkown() {
        assertThrows(TypeError.class, () -> eval("""
                @sensitiveUnknown
                output any something = null"""));

    }

    @Test
    void decoratorCountError() {
        assertThrows(TypeError.class, () -> eval("""
                @count(2)
                output any something = null"""));

    }

    @Test
    void decoratorCountErrorBoolean() {
        assertThrows(TypeError.class, () -> eval("""
                @count(true)
                output any something = null"""));

    }

    @Test
    void decoratorCountErrorString() {
        assertThrows(TypeError.class, () -> eval("""
                @count("2")
                output any something = null"""));

    }

    @Test
    void decoratorCountErrorArrayString() {
        assertThrows(TypeError.class, () -> eval("""
                @count(["2"])
                output any something = null"""));

    }

    @Test
    void decoratorCountErrorArrayNumber() {
        assertThrows(TypeError.class, () -> eval("""
                @count([2])
                output any something = null"""));

    }

    @Test
    void decoratorCountErrorObject() {
        assertThrows(TypeError.class, () -> eval("""
                @count({env: 2})
                output any something = null"""));

    }

    @Test
    void decoratorCount() {
        var res = eval("""
                schema vm {}
                @count(2)
                resource vm something {}""");

    }

    @Test
    void decoratorCountMax() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @count(1000)
                component vm {}""")
        );
    }

    @Test
    void decoratorCountMin() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @count(-10)
                component vm {}""")
        );
    }

    @Test
    void decoratorCountZero() {
        eval("""
                @count(0)
                component vm {}""");
    }

    @Test
    @DisplayName("decorator @count with decimal value rounds it to the floor")
    void decoratorCountZeroDecimal() {
        eval("""
                @count(0.7)
                component vm {}""");
    }

    @Test
    void decoratorDescriptionResource() {
        var res = eval("""
                schema vm {}
                @description("some markdown")
                resource vm something {}""");

    }

    @Test
    void decoratorDescriptionComponent() {
        var res = eval("""
                @description("some markdown")
                component something {}""");
    }

    @Test
    @DisplayName("decorator @description only allows strings as arguments")
    void decoratorDescriptionError() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @description(10)
                component something {}""")
        );
    }

    @Test
    void decoratorDescriptionVar() {
        var res = eval("""
                @description("some markdown")
                var something = 2""");
    }

    @Test
    void decoratorDescriptionSchema() {
        var res = eval("""
                @description("some markdown")
                schema something{}""");
    }

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

    @Test
    void decoratorAllowMissingNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed
                input string something"""));
    }

    @Test
    void decoratorAllow() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input string something"""));
    }

    @Test
    void decoratorAllowStrings() {
        eval("""
                @allowed(["hello", "world"])
                input string something""");
    }

    @Test
    void decoratorAllowNumber() {
        eval("""
                @allowed([10, 20])
                input number something""");
    }

    @Test
    void decoratorAllowStringsArray() {
        eval("""
                @allowed(["hello", "world"])
                input string[] something""");
    }

    @Test
    void decoratorAllowNumberArray() {
        eval("""
                @allowed([10, 20])
                input number[] something""");
    }

    @Test
    void decoratorAllowArray() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input string[] something"""));
    }

    @Test
    void decoratorAllowArrayNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input number[] something"""));
    }

    @Test
    void decoratorAllowArrayAny() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input any[] something"""));
    }

    @Test
    void decoratorAllowArrayObject() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input object[] something"""));
    }

    @Test
    void decoratorAllowTrueArray() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed(true)
                input object[] something"""));
    }

    @Test
    void decoratorAllowSchema() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed
                schema something{}"""));
    }

    @Test
    void decoratorAllowNegative() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @allowed(-10)
                input string something"""));
    }

}
