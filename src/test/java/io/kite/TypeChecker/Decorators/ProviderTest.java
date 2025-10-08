package io.kite.TypeChecker.Decorators;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.TypeError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("@provider")
public class ProviderTest extends CheckerTest {

    @Test
    void provider() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @provider
                resource vm something {}"""));
    }

    @Test
    void providerEmpty() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @provider()
                resource vm something {}"""));
    }

    @Test
    void providerEmptyList() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @provider([])
                resource vm something {}"""));
    }

    @Test
    void providerEmptyString() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @provider("")
                resource vm something {}"""));
    }

    @Test
    void providerEmptyStringArray() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @provider(["aws",10])
                resource vm something {}"""));
    }

    @Test
    void providerNumber() {
        Assertions.assertThrows(TypeError.class, () ->
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

}
