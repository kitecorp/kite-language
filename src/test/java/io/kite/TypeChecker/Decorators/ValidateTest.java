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
                        @validate(regex="^[a-z0-9-]+$")
                        input number something"""
                )
        );
        Assertions.assertEquals("\u001B[33m@validate\u001B[m is not allowed on number", error.getMessage());
    }

    @Test
    void validateInvalidRegex() {
        var error = Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex=1000)
                        input string something"""
                )
        );
        Assertions.assertEquals("regex argument must be a string literal for \u001B[33m@validate\u001B[m", error.getMessage());
    }

    @Test
    void validateBoolean() {
        var error = Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input boolean something""")
        );
        Assertions.assertEquals("\u001B[33m@validate\u001B[m is not allowed on boolean", error.getMessage());
    }

    @Test
    void validateAny() {
        var error = Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input any something""")
        );
        Assertions.assertEquals("\u001B[33m@validate\u001B[m is not allowed on any", error.getMessage());
    }

    @Test
    void validateObject() {
        var error = Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input object something""")
        );
        Assertions.assertEquals("\u001B[33m@validate\u001B[m is not allowed on object", error.getMessage());
    }

    @Test
    void validateStringArray() {
        eval("""
                @validate(regex="^[a-z0-9-]+$")
                input string[] something""");
    }


    @Test
    void validateNumberArray() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input number[] something"""
                )
        );
    }

    @Test
    void validateBooleanArray() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input boolean[] something"""
                )
        );
    }

    @Test
    void validateAnyArray() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input any[] something"""
                )
        );
    }

    @Test
    void validateObjectArray() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input object[] something"""
                )
        );
    }

}
