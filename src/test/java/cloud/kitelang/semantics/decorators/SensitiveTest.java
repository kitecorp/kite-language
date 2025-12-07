package cloud.kitelang.semantics.Decorators;

import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.semantics.types.AnyType;
import cloud.kitelang.semantics.types.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.semantics.typechecker.ComponentTest.assertIsComponentType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("@sensitive decorator")
public class SensitiveTest extends CheckerTest {

    @Test
    void decoratorSensitive() {
        var res = eval("""
                @sensitive
                output any something = null""");

        assertEquals(AnyType.INSTANCE, res);
    }

    @Test
    void decoratorSensitiveInvalidArgs() {
        assertThrows(TypeError.class, () -> eval("""
                @sensitive(2)
                output any something = null
                """));
    }

    @Test
    void decoratorSensitiveInvalidElement() {
        assertThrows(TypeError.class, () -> eval("""
                @sensitive
                var x = 2"""));
    }

    @Test
    void decoratorUnkown() {
        assertThrows(TypeError.class, () -> eval("""
                @sensitiveUnknown
                output any something = null"""));

    }

    @Test
    void decoratorSensitiveInComponent() {
        var res = eval("""
                component app {
                    @sensitive
                    output string apiKey = "secret"
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        var outputType = appComponent.lookup("apiKey");
        assertEquals(ValueType.String, outputType);
    }

    @Test
    void decoratorSensitiveOnInput() {
        var res = eval("""
                @sensitive
                input string password = "secret"
                """);

        assertEquals(ValueType.String, res);
    }

    @Test
    void decoratorSensitiveOnInputInComponent() {
        var res = eval("""
                component app {
                    @sensitive
                    input string password
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        var inputType = appComponent.lookup("password");
        assertEquals(ValueType.String, inputType);
    }

    @Test
    void decoratorSensitiveOnResourceShouldFail() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @sensitive
                resource vm server {}
                """));
    }

    @Test
    void decoratorSensitiveOnSchemaShouldFail() {
        assertThrows(TypeError.class, () -> eval("""
                @sensitive
                schema vm {}
                """));
    }

    @Test
    void decoratorSensitiveOnComponentShouldFail() {
        assertThrows(TypeError.class, () -> eval("""
                @sensitive
                component app {}
                """));
    }

}
