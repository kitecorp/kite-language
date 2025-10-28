package io.kite.Runtime.Decorators;

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
        assertEquals("Use letters, numbers, dashes for `input string something = \"bucket.\"`. Invalid value: \"bucket.\"", err.getMessage());
    }

    @Test
    void stringUpperCase() {
        var err = assertThrows(IllegalArgumentException.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$", message="Use letters, numbers, dashes")
                input string something = "Bucket"
                """));
        assertEquals("Use letters, numbers, dashes for `input string something = \"Bucket\"`. Invalid value: \"Bucket\"", err.getMessage());
    }

    @Test
    void presetAndRegexAreMutuallyExclusive() {
        var err = assertThrows(IllegalArgumentException.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$", preset="dns_label")
                input string something = "Bucket"
                """));
        assertEquals("@validate: use either 'preset' or 'regex', not both", err.getMessage());
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
    void validateSimpleOk() {
        eval("""
                    @validate(regex="^[a-z0-9-]+$")
                    input string name = "api-01"
                """);
    }

    @Test
    void validateSimpleFail() {
        assertThrows(IllegalArgumentException.class, () -> eval("""
                    @validate(regex="^[a-z0-9-]+$")
                    input string name = "Api_01"
                """));
    }

    @Test
    void validateFlagsCaseInsensitiveOk() {
        eval("""
                    @validate(regex="^[a-z0-9-]+$", flags="i")
                    input string name = "Api-01"
                """);
    }

    @Test
    void validateArrayAllOk() {
        eval("""
                    @validate(regex="^env-[a-z]+$")
                    input string[] names = ["env-dev","env-prod"]
                """);
    }

    @Test
    void validateArrayPointsToOffender() {
        var ex = assertThrows(IllegalArgumentException.class, () -> eval("""
                    @validate(regex="^env-[a-z]+$")
                    input string[] names = ["env-dev","prod"]
                """));
        assertTrue(ex.getMessage().contains("index 1"));
        assertTrue(ex.getMessage().contains("\"prod\""));
    }

    @Test
    void validateInvalidRegexRejectedEarly() {
        assertThrows(ParseError.class, () -> eval("""
                    @validate(regex="(unclosed")
                    input string name
                """));
    }

    @Test
    void presetDnsLabelEdgesLeadingDash() {
        assertThrows(IllegalArgumentException.class, () -> eval("""
                    @validate(preset="dns_label")
                    input string name = "-api"   // leading dash
                """));
    }

    @Test
    void presetDnsLabelEdgesTrailingDash() {
        assertThrows(IllegalArgumentException.class, () -> eval("""
                    @validate(preset="dns_label")
                    input string name = "api-"   // trailing dash
                """));
    }

    @Test
    void presetRfc1123SubdomainOk() {
        eval("""
                    @validate(preset="rfc1123")
                    input string host = "a.b-c.example-01.com"
                """);
    }

    @Test
    void presetRfc1123Over253Fails() {
        String longHost = "a.".repeat(127) + "a"; // >253 total
        assertThrows(IllegalArgumentException.class, () -> eval("""
                    @validate(preset="rfc1123")
                    input string host = "%s"
                """.formatted(longHost)));
    }

    @Test
    void presetKebabOk() {
        eval("""
                    @validate(preset="kebab")
                    input string svc = "api-v2"
                """);
    }

    @Test
    void presetKebabNotOkDoubleDash() {
        assertThrows(IllegalArgumentException.class, () -> eval("""
                    @validate(preset="kebab")
                    input string svc = "api--v2"
                """));
    }


    @Test
    void presetArrayElementsChecked() {
        var ex = assertThrows(IllegalArgumentException.class, () -> eval("""
                    @validate(preset="dns_label")
                    input string[] names = ["ok", "Bad"]
                """));
        assertTrue(ex.getMessage().contains("index 1"));
    }

    @Test
    void validateWithEmptyRegex() {
        var error = assertThrows(IllegalArgumentException.class, () -> eval("""
                @validate(regex="")
                input string something
                """));
        // Should empty regex be allowed?
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
