package io.kite.TypeChecker.Decorators;

import io.kite.Base.CheckerTest;
import io.kite.Frontend.Parser.errors.ErrorList;
import io.kite.Frontend.Parser.errors.ParseError;
import io.kite.TypeChecker.TypeError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("@provider")
public class ProviderTest extends CheckerTest {

    @Test
    void provider() {
        assertThrows(TypeError.class, () -> eval("""
                @provider
                resource vm something {}"""));
    }

    @Test
    void providerEmpty() {
        assertThrows(TypeError.class, () -> eval("""
                @provider()
                resource vm something {}"""));
    }

    @Test
    void providerEmptyList() {
        assertThrows(TypeError.class, () -> eval("""
                @provider([])
                resource vm something {}"""));
    }

    @Test
    void providerEmptyStringArray() {
        assertThrows(TypeError.class, () -> eval("""
                @provider(["aws",10])
                resource vm something {}"""));
    }

    @Test
    void providerNumber() {
        assertThrows(TypeError.class, () ->
                eval("""
                        @provider(10)
                        resource vm something {}""")
        );
    }

    @Test
    void providerValidString() {
        eval("""
                schema vm {}
                @provider("aws")
                resource vm something {}""");
    }

    @Test
    void providerValidStringArray() {
        eval("""
                schema vm {}
                @provider(["aws", "azure"])
                resource vm something {}""");
    }

    @Test
    void providerOnComponent() {
        assertThrows(TypeError.class, () -> eval("""
                @provider
                component app {}
                """));
    }

    @Test
    void providerEmptyOnComponent() {
        assertThrows(TypeError.class, () -> eval("""
                @provider()
                component app {}
                """));
    }

    @Test
    void providerEmptyListOnComponent() {
        assertThrows(TypeError.class, () -> eval("""
                @provider([])
                component app {}
                """));
    }

    @Test
    void providerEmptyStringOnComponent() {
        assertThrows(TypeError.class, () -> eval("""
                @provider("")
                component app {}
                """));
    }

    @Test
    void providerEmptyStringArrayOnComponent() {
        assertThrows(TypeError.class, () -> eval("""
                @provider(["aws", 10])
                component app {}
                """));
    }

    @Test
    void providerNumberOnComponent() {
        assertThrows(TypeError.class, () -> eval("""
                @provider(10)
                component app {}
                """));
    }

    @Test
    void providerValidStringOnComponent() {
        eval("""
                component app {}
                
                @provider("aws")
                component app prodApp {}
                """);
    }

    @Test
    void providerValidStringArrayOnComponent() {
        eval("""
                component app {}
                
                @provider(["aws", "azure"])
                component app prodApp {}
                """);
    }

    @Test
    void providerOnComponentDefinitionShouldFail() {
        assertThrows(TypeError.class, () -> eval("""
                @provider("aws")
                component app {}
                """));
    }

    @Test
    void providerOnResourceInComponent() {
        eval("""
                schema vm {}
                
                component app {
                    @provider("aws")
                    resource vm server {}
                }
                """);
    }

    @Test
    void providerOnResourceInComponentInstance() {
        eval("""
                schema vm {}
                
                component app {
                    @provider("aws")
                    resource vm server {}
                }
                
                component app prodApp {}
                """);
    }

    @Test
    void providerBoolean() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @provider(true)
                resource vm something {}
                """));
    }

    @Test
    void providerObject() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @provider({})
                resource vm something {}
                """));
    }

    @Test
    void providerArrayWithEmptyString() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @provider(["aws", ""])
                resource vm something {}
                """));
    }

    @Test
    void providerWhitespaceOnly() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @provider(" ")
                resource vm something {}
                """));
    }


    @Test
    void providerArrayWithWhitespace() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @provider([" ", "aws"])
                resource vm something {}
                """));
    }

    @Test
    void providerEmptyString() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @provider("")
                resource vm something {}
                """));
    }

    @Test
    void providerMultipleArguments() {
        assertThrows(ErrorList.class, () -> eval("""
                schema vm {}
                @provider("aws", "azure")
                resource vm something {}
                """));
    }

    @Test
    void providerNull() {
        assertThrows(ParseError.class, () -> eval("""
                schema vm {}
                @provider(null)
                resource vm something {}
                """));
    }

    @Test
    void providerOnInput() {
        assertThrows(TypeError.class, () -> eval("""
                @provider("aws")
                input string something
                """));
    }

    @Test
    void providerOnOutput() {
        assertThrows(TypeError.class, () -> eval("""
                @provider("aws")
                output string something = "test"
                """));
    }

    @Test
    void providerOnSchema() {
        assertThrows(TypeError.class, () -> eval("""
                @provider("aws")
                schema vm {}
                """));
    }

    @Test
    void providerWithVariable() {
        eval("""
                schema vm {}
                var cloud = "aws"
                @provider(cloud)
                resource vm something {}
                """);
    }

    @Test
    void providerWithInput() {
        eval("""
                schema vm {}
                component app {
                    input string cloud = "aws"
                    @provider(cloud)
                    resource vm server {}
                }
                """);
    }

    @Test
    void providerWithInvalidVariable() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                var cloud = 123
                @provider(cloud)
                resource vm something {}
                """));
    }

    @Test
    void providerDuplicatesInArray() {
        eval("""
                schema vm {}
                @provider(["aws", "aws", "azure"])
                resource vm something {}
                """);
        // Should this be valid or throw? Depends on your semantics
    }

    @Test
    void providerOnCountedResource() {
        eval("""
                schema vm {}
                @count(2)
                @provider("aws")
                resource vm something {}
                """);
    }

    @Test
    void providerOnResourceWithDependsOn() {
        eval("""
                schema vm {}
                resource vm first {}
                
                @provider("aws")
                @dependsOn(first)
                resource vm second {}
                """);
    }

    @Test
    void multipleResourcesDifferentProvidersInComponent() {
        eval("""
                schema vm {}
                component app {
                    @provider("aws")
                    resource vm server {}
                
                    @provider("azure")
                    resource vm database {}
                }
                """);
    }

    @Test
    void componentInstanceAndResourceDifferentProviders() {
        eval("""
                schema vm {}
                component app {
                    @provider("azure")
                    resource vm server {}
                }
                
                @provider("aws")
                component app prodApp {}
                """);
    }

    @Test
    void providerCaseSensitive() {
        eval("""
                schema vm {}
                @provider("AWS")
                resource vm something {}
                """);
        // Should "AWS" and "aws" be treated differently?
    }

    @Test
    void providerSpecialCharacters() {
        eval("""
                schema vm {}
                @provider("aws-us-east-1")
                resource vm something {}
                """);
    }

    @Test
    void providerVeryLongName() {
        eval("""
                schema vm {}
                @provider("a-very-long-provider-name-that-goes-on-and-on")
                resource vm something {}
                """);
    }

}
