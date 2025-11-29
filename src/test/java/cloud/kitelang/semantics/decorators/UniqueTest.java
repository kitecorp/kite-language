package cloud.kitelang.semantics.Decorators;

import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.syntax.ast.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("@unique")
public class UniqueTest extends CheckerTest {

    @Test
    void uniqueInvalidArgs() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @unique(10)
                input string something"""));
        assertEquals("@unique(10) must not have any arguments", error.getMessage());
    }

    @Test
    void uniqueString() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @unique
                input string something"""));
        assertEquals("\u001B[33m@unique\u001B[m is only valid for arrays. Applied to: \u001B[34mstring", error.getMessage());
    }

    @Test
    void uniqueNumber() {
        var error = assertThrows(TypeError.class, () ->
                eval("""
                        @unique
                        input number something""")
        );
        assertEquals("\u001B[33m@unique\u001B[m is only valid for arrays. Applied to: \u001B[34mnumber", error.getMessage());
    }

    @Test
    void uniqueBoolean() {
        var error = assertThrows(TypeError.class, () ->
                eval("""
                        @unique
                        input boolean something""")
        );
        assertEquals("\u001B[33m@unique\u001B[m is only valid for arrays. Applied to: \u001B[34mboolean", error.getMessage());
    }

    @Test
    void uniqueAny() {
        var error = assertThrows(TypeError.class, () ->
                eval("""
                        @unique
                        input any something""")
        );
        assertEquals("\u001B[33m@unique\u001B[m is only valid for arrays. Applied to: \u001B[34many", error.getMessage());
    }

    @Test
    void uniqueObject() {
        var error = assertThrows(TypeError.class, () ->
                eval("""
                        @unique
                        input object something""")
        );
        assertEquals("\u001B[33m@unique\u001B[m is only valid for arrays. Applied to: \u001B[34mobject", error.getMessage());
    }

    @Test
    void uniqueStringArray() {
        eval("""
                @unique
                input string[] something""");
    }


    @Test
    void uniqueNumberArray() {
        eval("""
                @unique
                input number[] something""");
    }

    @Test
    void uniqueBooleanArray() {
        eval("""
                @unique
                input boolean[] something""");
    }

    @Test
    void uniqueAnyArray() {
        eval("""
                @unique
                input any[] something""");
    }

    @Test
    void uniqueObjectArray() {
        eval("""
                @unique
                input object[] something""");
    }

    @Test
    void uniqueWithEmptyParentheses() {
        eval("""
                @unique()
                input string[] something
                """);
    }

    @Test
    void uniqueWithMultipleArgs() {
        var err = assertThrows(ValidationException.class, () -> eval("""
                @unique(1, 2)
                input string[] something
                """));
        assertEquals("""
                Parse error at line 1:9 - no viable alternative at input '@unique(1,'
                  @unique(1, 2)
                           ^
                """.trim(), err.getMessage());
    }

    @Test
    void uniqueWithStringArg() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @unique("test")
                input string[] something
                """));
        assertEquals("@unique(\"test\") must not have any arguments", error.getMessage());
    }

    @Test
    void uniqueWithBooleanArg() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @unique(true)
                input string[] something
                """));
        assertEquals("@unique(true) must not have any arguments", error.getMessage());
    }

    @Test
    void uniqueWithArrayArg() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @unique([1, 2])
                input string[] something
                """));
        assertEquals("@unique([1, 2]) must not have any arguments", error.getMessage());
    }

    @Test
    void uniqueWithObjectArg() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @unique({key: "value"})
                input string[] something
                """));
        assertEquals("@unique({\n" +
                     " \"key\": \"value\" \n" +
                     "}) must not have any arguments", error.getMessage());
    }

    @Test
    void uniqueOnOutput() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @unique
                output string[] something = ["a", "b"]
                """));
        // Assuming @unique is only for inputs - check your implementation
    }

    @Test
    void uniqueOnResource() {
        var error = assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @unique
                resource vm something {}
                """));
    }

    @Test
    void uniqueOnComponent() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @unique
                component app {}
                """));
    }

    @Test
    void uniqueOnSchema() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @unique
                schema vm {}
                """));
    }

    @Test
    void uniqueInComponent() {
        eval("""
                component app {
                    @unique
                    input string[] items
                }
                """);
    }

    @Test
    void uniqueOnInputInComponent() {
        eval("""
                component app {
                    @unique
                    input number[] values = [1, 2, 3]
                }
                """);
    }

    @Test
    void uniqueWithDefaultValue() {
        eval("""
                @unique
                input string[] items = ["a", "b", "c"]
                """);
    }

    @Test
    void uniqueWithEmptyArrayDefault() {
        eval("""
                @unique
                input string[] items = []
                """);
    }

    @Test
    void uniqueOnUnionArray() {
        eval("""
                type custom = string | number
                @unique
                input custom[] items
                """);
    }

    @Test
    void uniqueWithTypeMismatchDefault() {
        var error = assertThrows(TypeError.class, () -> eval("""
                @unique
                input string[] items = [1, 2, 3]
                """));
        // Should fail on type mismatch, not @unique
    }

}
