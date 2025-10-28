package io.kite.Integration.Decorators;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.TypeError;
import io.kite.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.Test;

import static io.kite.TypeChecker.ComponentTest.assertIsComponentType;
import static org.junit.jupiter.api.Assertions.*;

public class ComponentAllowedTest extends CheckerTest {
    @Test
    void componentInputWithAllowedDecorator() {
        var res = eval("""
                component app {
                    @allowed(["hello", "world"])
                    input string something
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        var inputType = appComponent.lookup("something");
        assertNotNull(inputType);
        assertEquals(ValueType.String, inputType);
    }

    @Test
    void componentInputWithAllowedDecoratorValidValue() {
        var res = eval("""
                component app {
                    @allowed(["hello", "world"])
                    input string something = "hello"
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        var inputType = appComponent.lookup("something");
        assertEquals(ValueType.String, inputType);
    }

    @Test
    void componentInstanceInputWithAllowedDecoratorValidValue() {
        var res = eval("""
                component app {
                    @allowed(["hello", "world"])
                    input string something
                }
                
                component app prodApp {
                    something = "hello"
                }
                """);

        var prodAppInstance = assertIsComponentType(res, "app");
        assertEquals("prodApp", prodAppInstance.getName());
    }

    @Test
    void componentInputWithAllowedDecoratorWrongType() {
        assertThrows(TypeError.class, () -> eval("""
                component app {
                    @allowed(["hello", "world"])
                    input string something = 123
                }
                """));
    }

    @Test
    void componentInstanceInputWithAllowedDecoratorInvalidValue() {
        assertThrows(TypeError.class, () -> eval("""
                component app {
                    @allowed(["hello", "world"])
                    input string something
                }
                
                component app prodApp {
                    something = 123
                }
                """));
    }
}
