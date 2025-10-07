package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.ParserErrors;
import io.kite.Frontend.Parser.errors.ParseError;
import io.kite.TypeChecker.TypeError;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class ValidateTests extends DecoratorTests {

    @Test
    void stringHappyFlow() {
        eval("""
                @validate(regex="^[a-z0-9-]+$", flags="i", message="Use letters, numbers, dashes")
                input string something = "bucket"
                """);
    }

    @Test
    void stringSymbol() {
        var err = assertThrows(IllegalArgumentException.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$", message="Use letters, numbers, dashes")
                input string something = "bucket."
                """));
        assertEquals("Use letters, numbers, dashes for `\u001B[m\u001B[2J\u001B[35minput \u001B[34mstring\u001B[39m \u001B[39msomething = \"bucket.\"`. Invalid value: \"bucket.\"", err.getMessage());
    }

    @Test
    void stringUpperCase() {
        var err = assertThrows(IllegalArgumentException.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$", message="Use letters, numbers, dashes")
                input string something = "Bucket"
                """));
        assertEquals("Use letters, numbers, dashes for `\u001B[m\u001B[2J\u001B[35minput \u001B[34mstring\u001B[39m \u001B[39msomething = \"Bucket\"`. Invalid value: \"Bucket\"", err.getMessage());
    }

    @Test
    void stringUpperCaseIgnored() {
        eval("""
                @validate(regex="^[a-z0-9-]+$", flags= "i", message="Use letters, numbers, dashes")
                input string something = "Bucket"
                """);
    }

    @Test
    void stringStringArray() {
        eval("""
                @validate(regex="^[a-z0-9-]+$")
                input string[] something = ["api-01", "db-01"]
                """);
    }

    @Test
    void stringArray() {
        eval("""
                @validate(regex="^[a-z0-9-]+$")
                input string[] something = ["hello"]
                """);
    }

    @Test
    void stringArrayMultiple() {
        eval("""
                @validate(regex="^[a-z0-9-]+$")
                input string[] something = ["hello", "world"]
                """);
    }

    @Test
    void NumberArrayEmpty() {
        var err = assertThrows(TypeError.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$")
                input number[] something = []
                """));
        assertEquals("\u001B[33m@validate\u001B[m is not allowed on number", err.getMessage());
    }

    @Test
    void anyArrayEmpty() {
        var err = assertThrows(TypeError.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$")
                input any[] something = []
                """));
        assertEquals("\u001B[33m@validate\u001B[m is not allowed on any", err.getMessage());
    }

    @Test
    void objectArrayEmpty() {
        var err = assertThrows(TypeError.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$")
                input object[] something = []
                """));
        assertEquals("\u001B[33m@validate\u001B[m is not allowed on object", err.getMessage());
    }

    @Test
    void validate_simple_ok() {
        eval("""
                    @validate(regex="^[a-z0-9-]+$")
                    input string name = "api-01"
                """);
    }

    @Test
    void validate_simple_fail() {
        assertThrows(IllegalArgumentException.class, () -> eval("""
                    @validate(regex="^[a-z0-9-]+$")
                    input string name = "Api_01"
                """));
    }

    @Test
    void validate_flags_caseInsensitive_ok() {
        eval("""
                    @validate(regex="^[a-z0-9-]+$", flags="i")
                    input string name = "Api-01"
                """);
    }

    @Test
    void validate_array_all_ok() {
        eval("""
                    @validate(regex="^env-[a-z]+$")
                    input string[] names = ["env-dev","env-prod"]
                """);
    }

    @Test
    void validate_array_points_to_offender() {
        var ex = assertThrows(IllegalArgumentException.class, () -> eval("""
                    @validate(regex="^env-[a-z]+$")
                    input string[] names = ["env-dev","prod"]
                """));
        assertTrue(ex.getMessage().contains("index 1"));
        assertTrue(ex.getMessage().contains("\"prod\""));
    }

    @Test
    void validate_invalid_regex_rejected_early() {
        assertThrows(ParseError.class, () -> eval("""
                    @validate(regex="(unclosed")
                    input string name
                """));
    }

//    @Test
//    void validate_runs_on_override() {
//        // default passes, CLI/env override fails: your harness should simulate override
//        withCliArg("name", "BAD!", () -> assertThrows(IllegalArgumentException.class, () -> eval("""
//                    @validate(regex="^[A-Z]+$")
//                    input string name = "OK"
//                """)));
//    }
}
