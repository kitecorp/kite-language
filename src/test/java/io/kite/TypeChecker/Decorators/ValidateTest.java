package io.kite.TypeChecker.Decorators;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.TypeError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("@validate")
public class ValidateTest extends CheckerTest {

    @Test
    void validateInvalidArgs() {
        var error = Assertions.assertThrows(TypeError.class, () -> eval("""
                @validate(10)
                input string something"""));
        Assertions.assertEquals("\u001B[33m@validate\u001B[m must not have any arguments", error.getMessage());
    }

    @Test
    void validateMissingArgs() {
        var error = Assertions.assertThrows(TypeError.class, () -> eval("""
                @validate
                input string something"""));
        Assertions.assertEquals("Missing \u001B[33m@validate\u001B[m arguments!", error.getMessage());
    }

    @Test
    void validateString() {
        eval("""
                @validate(regex="^[a-z0-9-]+$", flags="i", message="Use letters, numbers, dashes")
                input string something""");
    }

    @Test
    void validateMissingRegexArg() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @validate(flags="i", message="Use letters, numbers, dashes")
                        input string something""")
        );
    }

    @Test
    void validateNumber() {
        var error = Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$", flags="i", message="Use letters, numbers, dashes")
                        input number something"""
                )
        );
        Assertions.assertEquals("\u001B[33m@validate\u001B[m is only valid for arrays. Applied to: \u001B[34mnumber", error.getMessage());
    }

    @Test
    void validateBoolean() {
        var error = Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @validate
                        input boolean something""")
        );
        Assertions.assertEquals("\u001B[33m@validate\u001B[m is only valid for arrays. Applied to: \u001B[34mboolean", error.getMessage());
    }

    @Test
    void validateAny() {
        var error = Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @validate
                        input any something""")
        );
        Assertions.assertEquals("\u001B[33m@validate\u001B[m is only valid for arrays. Applied to: \u001B[34many", error.getMessage());
    }

    @Test
    void validateObject() {
        var error = Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @validate
                        input object something""")
        );
        Assertions.assertEquals("\u001B[33m@validate\u001B[m is only valid for arrays. Applied to: \u001B[34mobject", error.getMessage());
    }

    @Test
    void validateStringArray() {
        eval("""
                @validate
                input string[] something""");
    }


    @Test
    void validateNumberArray() {
        eval("""
                @validate
                input number[] something""");
    }

    @Test
    void validateBooleanArray() {
        eval("""
                @validate
                input boolean[] something""");
    }

    @Test
    void validateAnyArray() {
        eval("""
                @validate
                input any[] something""");
    }

    @Test
    void validateObjectArray() {
        eval("""
                @validate
                input object[] something""");
    }

}
