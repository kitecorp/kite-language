package cloud.kitelang.execution.decorators;

import cloud.kitelang.execution.InputEnvVariableTests;
import cloud.kitelang.execution.exceptions.MissingInputException;
import cloud.kitelang.semantics.TypeError;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
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
        assertEquals("Use letters, numbers, dashes for `input string region = \"bucket.\"`. Invalid value: \"bucket.\"", err.getMessage());
    }

    @Test
    void stringUpperCase() {
        setInput("Bucket");
        var err = assertThrows(IllegalArgumentException.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$", message="Use letters, numbers, dashes")
                input string region 
                """));
        assertEquals("Use letters, numbers, dashes for `input string region = \"Bucket\"`. Invalid value: \"Bucket\"", err.getMessage());
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
        assertEquals("@validate(regex = \"^[a-z0-9-]+$\") is not allowed on number", err.getMessage());
    }

    @Test
    void anyArrayEmpty() {
        setInput("[]");
        var err = assertThrows(TypeError.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$")
                input any[] region
                """));
        assertEquals("@validate(regex = \"^[a-z0-9-]+$\") is not allowed on any", err.getMessage());
    }

    @Test
    void objectArrayEmpty() {
        setInput("[]");
        var err = assertThrows(TypeError.class, () -> eval("""
                @validate(regex="^[a-z0-9-]+$")
                input object[] region
                """));
        assertEquals("@validate(regex = \"^[a-z0-9-]+$\") is not allowed on object", err.getMessage());
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
        assertThrows(MissingInputException.class, () -> eval("""
                    @validate(regex="(unclosed")
                    input string name
                """));
    }

    @Test
    void validateInComponentHappyFlow() {
        eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$", flags="i", message="Use letters, numbers, dashes")
                    input string region
                }
                
                component app prod {
                    region = "bucket"
                }
                """);
    }

    @Test
    void validateInComponentWithSymbol() {
        var err = assertThrows(IllegalArgumentException.class, () -> eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$", message="Use letters, numbers, dashes")
                    input string region
                }
                
                component app prod {
                    region = "bucket."
                }
                """));
        assertTrue(err.getMessage().contains("Use letters, numbers, dashes"));
        assertTrue(err.getMessage().contains("bucket."));
    }

    @Test
    void validateInComponentUpperCase() {
        var err = assertThrows(IllegalArgumentException.class, () -> eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$", message="Use letters, numbers, dashes")
                    input string region
                }
                
                component app prod {
                    region = "Bucket"
                }
                """));
        assertTrue(err.getMessage().contains("Use letters, numbers, dashes"));
        assertTrue(err.getMessage().contains("Bucket"));
    }

    @Test
    void validateInComponentUpperCaseIgnored() {
        eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$", flags="i", message="Use letters, numbers, dashes")
                    input string region
                }
                
                component app prod {
                    region = "Bucket"
                }
                """);
    }

    @Test
    void validateInComponentStringArray() {
        eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$")
                    input string[] regions
                }
                
                component app prod {
                    regions = ["api-01", "db-01"]
                }
                """);
    }

    @Test
    void validateInComponentStringArraySingle() {
        eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$")
                    input string[] regions
                }
                
                component app prod {
                    regions = ["hello"]
                }
                """);
    }

    @Test
    void validateInComponentStringArrayMultiple() {
        eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$")
                    input string[] regions
                }
                
                component app prod {
                    regions = ["hello", "world"]
                }
                """);
    }

    @Test
    void validateInComponentSimpleOk() {
        eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$")
                    input string region
                }
                
                component app prod {
                    region = "api-01"
                }
                """);
    }

    @Test
    void validateInComponentSimpleFail() {
        assertThrows(IllegalArgumentException.class, () -> eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$")
                    input string region
                }
                
                component app prod {
                    region = "Api_01"
                }
                """));
    }

    @Test
    void validateInComponentFlagsCaseInsensitiveOk() {
        eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$", flags="i")
                    input string region
                }
                
                component app prod {
                    region = "Api-01"
                }
                """);
    }

    @Test
    void validateInComponentArrayAllOk() {
        eval("""
                component app {
                    @validate(regex="^env-[a-z]+$")
                    input string[] regions
                }
                
                component app prod {
                    regions = ["env-dev", "env-prod"]
                }
                """);
    }

    @Test
    void validateInComponentArrayPointsToOffender() {
        var ex = assertThrows(IllegalArgumentException.class, () -> eval("""
                component app {
                    @validate(regex="^env-[a-z]+$")
                    input string[] regions
                }
                
                component app prod {
                    regions = ["env-dev", "prod"]
                }
                """));
        assertTrue(ex.getMessage().contains("index 1"));
        assertTrue(ex.getMessage().contains("prod"));
    }

    @Test
    void validateInComponentWithDefaultValue() {
        eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$")
                    input string region = "default-region"
                }
                
                component app prod {
                }
                """);
    }

    @Test
    void validateInComponentWithInvalidDefaultValue() {
        assertThrows(IllegalArgumentException.class, () -> eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$")
                    input string region = "Invalid_Region"
                }
                
                component app prod {
                }
                """));
    }

    @Test
    void validateInComponentOverrideValidDefault() {
        eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$")
                    input string region = "default-region"
                }
                
                component app prod {
                    region = "prod-region"
                }
                """);
    }

    @Test
    void validateInComponentOverrideWithInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$")
                    input string region = "default-region"
                }
                
                component app prod {
                    region = "Invalid_Region"
                }
                """));
    }

    @Test
    void validateInNestedComponent() {
        eval("""
                component outer {
                    component inner {
                        @validate(regex="^[a-z0-9-]+$")
                        input string name
                    }
                }
                """);
    }

    @Test
    void validateMultipleInputsInComponent() {
        eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$")
                    input string region
                
                    @validate(regex="^env-[a-z]+$")
                    input string environment
                }
                
                component app prod {
                    region = "us-east-1"
                    environment = "env-prod"
                }
                """);
    }

    @Test
    void validateMultipleInputsOneInvalid() {
        var ex = assertThrows(IllegalArgumentException.class, () -> eval("""
                component app {
                    @validate(regex="^[a-z0-9-]+$")
                    input string region
                
                    @validate(regex="^env-[a-z]+$")
                    input string environment
                }
                
                component app prod {
                    region = "us-east-1"
                    environment = "prod"
                }
                """));
        assertTrue(ex.getMessage().contains("environment") || ex.getMessage().contains("prod"));
    }

}
