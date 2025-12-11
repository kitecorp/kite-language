package cloud.kitelang.execution.struct;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.StructValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for struct instantiation.
 */
@DisplayName("Struct Instantiation")
public class StructInstantiationTest extends RuntimeTest {

    @Test
    @DisplayName("Create instance with positional arguments")
    void createInstanceWithPositionalArgs() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                Point(10, 20)
                """);

        assertInstanceOf(StructValue.class, result);
        var instance = (StructValue) result;
        assertEquals("Point", instance.getType());
        assertEquals(10, instance.get("x"));
        assertEquals(20, instance.get("y"));
    }

    @Test
    @DisplayName("Create instance with defaults")
    void createInstanceWithDefaults() {
        var result = eval("""
                struct Point {
                    number x = 0
                    number y = 0
                }
                Point(5)
                """);

        assertInstanceOf(StructValue.class, result);
        var instance = (StructValue) result;
        assertEquals(5, instance.get("x"));
        assertEquals(0, instance.get("y"));
    }

    @Test
    @DisplayName("Create instance with all defaults")
    void createInstanceWithAllDefaults() {
        var result = eval("""
                struct Config {
                    number port = 8080
                    string host = "localhost"
                }
                Config()
                """);

        assertInstanceOf(StructValue.class, result);
        var instance = (StructValue) result;
        assertEquals(8080, instance.get("port"));
        assertEquals("localhost", instance.get("host"));
    }

    @Test
    @DisplayName("Instance is separate from definition")
    void instanceIsSeparateFromDefinition() {
        var result = eval("""
                struct Point {
                    number x = 0
                    number y = 0
                }
                var p1 = Point(1, 2)
                var p2 = Point(3, 4)
                [p1.x, p1.y, p2.x, p2.y]
                """);

        assertInstanceOf(java.util.List.class, result);
        var list = (java.util.List<?>) result;
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
        assertEquals(4, list.get(3));
    }

    @Test
    @DisplayName("Error when missing required arguments")
    void errorWhenMissingRequiredArgs() {
        assertThrows(Exception.class, () -> eval("""
                struct Point {
                    number x
                    number y
                }
                Point(10)
                """));
    }

    @Test
    @DisplayName("Error when too many arguments")
    void errorWhenTooManyArgs() {
        assertThrows(Exception.class, () -> eval("""
                struct Point {
                    number x
                    number y
                }
                Point(10, 20, 30)
                """));
    }

    @Test
    @DisplayName("Instance properties are mutable")
    void instancePropertiesAreMutable() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                var p = Point(10, 20)
                p.x = 100
                p.x
                """);

        assertEquals(100, result);
    }

    @Test
    @DisplayName("Struct with mixed required and optional properties")
    void structWithMixedProperties() {
        var result = eval("""
                struct User {
                    string name
                    string email = "no-reply@example.com"
                    number age = 0
                }
                User("John")
                """);

        assertInstanceOf(StructValue.class, result);
        var instance = (StructValue) result;
        assertEquals("John", instance.get("name"));
        assertEquals("no-reply@example.com", instance.get("email"));
        assertEquals(0, instance.get("age"));
    }
}
