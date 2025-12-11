package cloud.kitelang.execution.struct;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.StructValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for struct auto-coercion from object literals.
 */
@DisplayName("Struct Auto-Coercion")
public class StructAutoCoercionTest extends RuntimeTest {

    @Test
    @DisplayName("Auto-coerce object literal to struct with type annotation")
    void autoCoerceObjectLiteralToStruct() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                var Point p = { x: 10, y: 20 }
                p
                """);

        assertInstanceOf(StructValue.class, result);
        var instance = (StructValue) result;
        assertEquals("Point", instance.getType());
        assertEquals(10, instance.get("x"));
        assertEquals(20, instance.get("y"));
    }

    @Test
    @DisplayName("Auto-coerce with defaults")
    void autoCoerceWithDefaults() {
        var result = eval("""
                struct Config {
                    number port = 8080
                    string host = "localhost"
                }
                var Config c = { port: 3000 }
                c
                """);

        assertInstanceOf(StructValue.class, result);
        var instance = (StructValue) result;
        assertEquals(3000, instance.get("port"));
        assertEquals("localhost", instance.get("host"));
    }

    @Test
    @DisplayName("Auto-coerce empty object when all fields have defaults")
    void autoCoerceEmptyObject() {
        var result = eval("""
                struct Config {
                    number port = 8080
                    string host = "localhost"
                }
                var Config c = {}
                c
                """);

        assertInstanceOf(StructValue.class, result);
        var instance = (StructValue) result;
        assertEquals(8080, instance.get("port"));
        assertEquals("localhost", instance.get("host"));
    }

    @Test
    @DisplayName("Error when auto-coercing with missing required property")
    void errorWhenMissingRequiredProperty() {
        assertThrows(Exception.class, () -> eval("""
                struct Point {
                    number x
                    number y
                }
                var Point p = { x: 10 }
                """));
    }

    @Test
    @DisplayName("Access properties on auto-coerced struct")
    void accessPropertiesOnAutoCoercedStruct() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                var Point p = { x: 10, y: 20 }
                p.x + p.y
                """);

        assertEquals(30, result);
    }

    @Test
    @DisplayName("Mutate auto-coerced struct")
    void mutateAutoCoercedStruct() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                var Point p = { x: 10, y: 20 }
                p.x = 100
                p.x
                """);

        assertEquals(100, result);
    }
}
