package io.kite.TypeChecker.Decorators;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.TypeError;
import io.kite.Visitors.PlainTheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("@validate")
public class ValidateTest extends CheckerTest {

    @Test
    void validateInvalidArgs() {
        checker.getPrinter().setTheme(new PlainTheme());
        var error = assertThrows(TypeError.class, () -> eval("""
                @validate(10)
                input string something"""
        ));
        assertEquals("@validate(10) can only have named arguments", error.getMessage());
    }

    @Test
    void validateMissingArgs() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @validate
                input string something"""
        ));
        assertEquals("Missing \u001B[33m@validate\u001B[m arguments!", error.getMessage());
    }

    @Test
    void validateString() {
        eval("""
                @validate(regex="^[a-z0-9-]+$", flags="i", message="Use letters, numbers, dashes")
                input string something"""
        );
    }

    @Test
    void validateMissingRegexArg() {
        assertThrows(TypeError.class, () ->
                eval("""
                        @validate(flags="i", message="Use letters, numbers, dashes")
                        input string something"""
                )
        );
    }

    @Test
    void validateNumber() {
        var error = assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input number something"""
                )
        );
        assertEquals("\u001B[33m@validate\u001B[m is not allowed on number", error.getMessage());
    }

    @Test
    void validateInvalidRegex() {
        var error = assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex=1000)
                        input string something"""
                )
        );
        assertEquals("regex argument must be a string literal for \u001B[33m@validate\u001B[m", error.getMessage());
    }

    @Test
    void validateBoolean() {
        var error = assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input boolean something"""
                )
        );
        assertEquals("\u001B[33m@validate\u001B[m is not allowed on boolean", error.getMessage());
    }

    @Test
    void validateAny() {
        var error = assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input any something"""
                )
        );
        assertEquals("\u001B[33m@validate\u001B[m is not allowed on any", error.getMessage());
    }

    @Test
    void validateObject() {
        var error = assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input object something""")
        );
        assertEquals("\u001B[33m@validate\u001B[m is not allowed on object", error.getMessage());
    }

    @Test
    void validateStringArray() {
        eval("""
                @validate(regex="^[a-z0-9-]+$")
                input string[] something"""
        );
    }


    @Test
    void validateNumberArray() {
        assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input number[] something"""
                )
        );
    }

    @Test
    void validateBooleanArray() {
        assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input boolean[] something"""
                )
        );
    }

    @Test
    void validateAnyArray() {
        assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input any[] something"""
                )
        );
    }

    @Test
    void validateObjectArray() {
        assertThrows(TypeError.class, () ->
                eval("""
                        @validate(regex="^[a-z0-9-]+$")
                        input object[] something"""
                )
        );
    }

    @Test
    void validateWithEmptyParentheses() {
        checker.getPrinter().setTheme(new PlainTheme());
        var error = assertThrows(TypeError.class, () -> eval("""
                @validate()
                input string something
                """));
        assertEquals("@validate is missing arguments!", error.getMessage());
    }

    @Test
    void validateWithOnlyFlags() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @validate(flags="i")
                input string something
                """));
        // Should fail - regex is required
    }

    @Test
    void validateWithOnlyMessage() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @validate(message="Invalid format")
                input string something
                """));
        // Should fail - regex is required
    }

    @Test
    void validateWithBooleanRegex() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @validate(regex=true)
                input string something
                """));
        assertEquals("regex argument must be a string literal for \u001B[33m@validate\u001B[m", error.getMessage());
    }

    @Test
    void validateWithArrayRegex() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @validate(regex=["test"])
                input string something
                """));
        assertEquals("regex argument must be a string literal for \u001B[33m@validate\u001B[m", error.getMessage());
    }

    @Test
    void validateWithObjectRegex() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @validate(regex={key: "value"})
                input string something
                """));
        assertEquals("regex argument must be a string literal for \u001B[33m@validate\u001B[m", error.getMessage());
    }


    @Test
    void validateOnResource() {
        var error = assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @validate(regex="^[a-z]+$")
                resource vm something {}
                """));
    }

    @Test
    void validateOnComponent() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @validate(regex="^[a-z]+$")
                component app {}
                """));
    }

    @Test
    void validateOnSchema() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @validate(regex="^[a-z]+$")
                schema vm {}
                """));
    }

    @Test
    void validateInComponent() {
        eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$", flags="i")
                    input string name
                }
                """);
    }

    @Test
    void validateWithDefaultValue() {
        eval("""
                @validate(regex="^[a-z0-9-]+$")
                input string name = "test-123"
                """);
    }

    @Test
    void validateOnStringArrayWithAllArgs() {
        eval("""
                @validate(regex="^[a-z0-9-]+$", flags="gi", message="Invalid format")
                input string[] items
                """);
    }

    @Test
    void validateWithUnknownNamedArgument() {
        var error = eval("""
                @validate(regex="^[a-z]+$", unknown="value")
                input string something
                """);
        // unknown args are just ignored
    }

}
