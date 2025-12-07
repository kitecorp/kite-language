package cloud.kitelang.semantics.Decorators;

import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.semantics.types.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.semantics.typechecker.ComponentTest.assertIsComponentType;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("@allowed decorator")
public class AllowedTest extends CheckerTest {

    @Test
    void decoratorAllowStrings() {
        eval("""
                @allowed(["hello", "world"])
                input string something""");
    }

    @Test
    void decoratorAllowNumber() {
        eval("""
                @allowed([10, 20])
                input number something""");
    }

    @Test
    void decoratorAllowStringsArray() {
        eval("""
                @allowed(["hello", "world"])
                input string[] something""");
    }

    @Test
    void decoratorAllowNumberArray() {
        eval("""
                @allowed([10, 20])
                input number[] something""");
    }

    @Test
    void decoratorAllowMissingNumber() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed
                input string something"""));
    }

    @Test
    void decoratorAllow() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input string something"""));
    }

    @Test
    void decoratorAllowArray() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input string[] something"""));
    }

    @Test
    void decoratorAllowArrayNumber() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input number[] something"""));
    }

    @Test
    void decoratorAllowArrayAny() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input any[] something"""));
    }

    @Test
    void decoratorAllowArrayObject() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(10)
                input object[] something"""));
    }

    @Test
    void decoratorAllowTrueArray() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(true)
                input object[] something"""));
    }

    @Test
    void decoratorAllowSchema() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed
                schema something{}"""));
    }

    @Test
    void decoratorAllowNegative() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(-10)
                input string something"""));
    }


    @Test
    void decoratorAllowNumberAny() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed([10, 20])
                input any something"""));
        // throws because explicit type is any and implicit type is missing
    }

    @Test
    void decoratorAllowStringAny() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(['hello', 'world'])
                input any something"""));
        // throws because explicit type is any and implicit type is missing
    }

    @Test
    void decoratorAllowBooleanAny() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed([true, false])
                input any something"""));
        // throws because explicit type is any and implicit type is missing
    }

    @Test
    void decoratorAllowObjectAny() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed([{env: dev}])
                input any something"""));
        // throws because explicit type is any and implicit type is missing
    }


    @Test
    void decoratorAllowedStringAssignNumber() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(["hello", "world"])
                input string something = 123
                """));
    }

    @Test
    void decoratorAllowedStringAssignBoolean() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(["hello", "world"])
                input string something = true
                """));
    }

    @Test
    void decoratorAllowedStringAssignObject() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(["hello", "world"])
                input string something = {}
                """));
    }

    @Test
    void decoratorAllowedStringAssignArray() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(["hello", "world"])
                input string something = [1, 2, 3]
                """));
    }

    @Test
    void decoratorAllowedNumberAssignString() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed([10, 20])
                input number something = "hello"
                """));
    }

    @Test
    void decoratorAllowedNumberAssignBoolean() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed([10, 20])
                input number something = true
                """));
    }

    @Test
    void decoratorAllowedNumberAssignObject() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed([10, 20])
                input number something = {}
                """));
    }

    @Test
    void decoratorAllowedStringArrayAssignNumberArray() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(["hello", "world"])
                input string[] something = [1, 2, 3]
                """));
    }

    @Test
    void decoratorAllowedStringArrayAssignString() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(["hello", "world"])
                input string[] something = "hello"
                """));
    }

    @Test
    void decoratorAllowedStringArrayAssignBooleanArray() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed(["hello", "world"])
                input string[] something = [true, false]
                """));
    }

    @Test
    void decoratorAllowedNumberArrayAssignStringArray() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed([10, 20])
                input number[] something = ["hello", "world"]
                """));
    }

    @Test
    void decoratorAllowedNumberArrayAssignNumber() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed([10, 20])
                input number[] something = 10
                """));
    }

    @Test
    void decoratorAllowedNumberArrayAssignBooleanArray() {
        assertThrows(TypeError.class, () -> eval("""
                @allowed([10, 20])
                input number[] something = [true, false]
                """));
    }

    @Test
    void decoratorAllowedValidStringAssignment() {
        eval("""
                @allowed(["hello", "world"])
                input string something = "hello"
                """);
    }

    @Test
    void decoratorAllowedValidNumberAssignment() {
        eval("""
                @allowed([10, 20])
                input number something = 10
                """);
    }

    @Test
    void decoratorAllowedValidStringArrayAssignment() {
        eval("""
                @allowed(["hello", "world"])
                input string[] something = ["hello"]
                """);
    }

    @Test
    void decoratorAllowedValidNumberArrayAssignment() {
        eval("""
                @allowed([10, 20])
                input number[] something = [10, 20]
                """);
    }

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
