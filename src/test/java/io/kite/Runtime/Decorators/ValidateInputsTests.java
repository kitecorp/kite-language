package io.kite.Runtime.Decorators;

import io.kite.Frontend.Parser.errors.ParseError;
import io.kite.Runtime.InputEnvVariableTests;
import io.kite.TypeChecker.TypeError;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class ValidateInputsTests extends InputEnvVariableTests {

    @Test
    void stringHappyFlow() {
        setInput("bucket");
        eval("""
                @validate(regex="^[a-z0-9-]+$", flags="i", message="Use letters, numbers, dashes")
                input string region
                """);
    }

    @Test
    void stringSymbol() {
        setInput("bucket.");
        var err = assertThrows(IllegalArgumentException.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$", message="Use letters, numbers, dashes")
                input string region
                """));
        assertEquals("Use letters, numbers, dashes for `\u001B[35minput \u001B[39m\u001B[34mstring\u001B[39m \u001B[39mregion\u001B[39m = \u001B[32m\"bucket.\"\u001B[39m`. Invalid value: \"bucket.\"", err.getMessage());
    }

    @Test
    void stringUpperCase() {
        setInput("Bucket");
        var err = assertThrows(IllegalArgumentException.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$", message="Use letters, numbers, dashes")
                input string region 
                """));
        assertEquals("Use letters, numbers, dashes for `\u001B[35minput \u001B[39m\u001B[34mstring\u001B[39m \u001B[39mregion\u001B[39m = \u001B[32m\"Bucket\"\u001B[39m`. Invalid value: \"Bucket\"", err.getMessage());
    }

    @Test
    void stringUpperCaseIgnored() {
        setInput("Bucket");
        eval("""
                @validate(regex="^[a-z0-9-]+$", flags= "i", message="Use letters, numbers, dashes")
                input string region 
                """);
    }

    @Test
    void stringStringArray() {
        setInput("[\"api-01\", \"db-01\"]");
        eval("""
                @validate(regex="^[a-z0-9-]+$")
                input string[] region 
                """);
    }

    @Test
    void stringArray() {
        setInput("[\"hello\"]");
        eval("""
                @validate(regex="^[a-z0-9-]+$")
                input string[] region 
                """);
    }

    @Test
    void stringArrayMultiple() {
        setInput("[\"hello\", \"world\"]");
        eval("""
                @validate(regex="^[a-z0-9-]+$")
                input string[] region 
                """);
    }

    @Test
    void NumberArrayEmpty() {
        setInput("[]");
        var err = assertThrows(TypeError.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$")
                input number[] region
                """));
        assertEquals("\u001B[33m@validate\u001B[m is not allowed on number", err.getMessage());
    }

    @Test
    void anyArrayEmpty() {
        setInput("[]");
        var err = assertThrows(TypeError.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$")
                input any[] region
                """));
        assertEquals("\u001B[33m@validate\u001B[m is not allowed on any", err.getMessage());
    }

    @Test
    void objectArrayEmpty() {
        setInput("[]");
        var err = assertThrows(TypeError.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$")
                input object[] region
                """));
        assertEquals("\u001B[33m@validate\u001B[m is not allowed on object", err.getMessage());
    }

    @Test
    void validate_simple_ok() {
        setInput("api-01");
        eval("""
                    @validate(regex="^[a-z0-9-]+$")
                    input string region 
                """);
    }

    @Test
    void validate_simple_fail() {
        setInput("Api_01");
        assertThrows(IllegalArgumentException.class, () -> eval("""
                    @validate(regex="^[a-z0-9-]+$")
                    input string region
                """));
    }

    @Test
    void validate_flags_caseInsensitive_ok() {
        setInput("Api-01");
        eval("""
                    @validate(regex="^[a-z0-9-]+$", flags="i")
                    input string region
                """);
    }

    @Test
    void validate_array_all_ok() {
        setInput("[\"env-dev\",\"env-prod\"]");
        eval("""
                    @validate(regex="^env-[a-z]+$")
                    input string[] region
                """);
    }

    @Test
    void validate_array_points_to_offender() {
        setInput("[\"env-dev\",\"prod\"]");
        var ex = assertThrows(IllegalArgumentException.class, () -> eval("""
                    @validate(regex="^env-[a-z]+$")
                    input string[] region
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
