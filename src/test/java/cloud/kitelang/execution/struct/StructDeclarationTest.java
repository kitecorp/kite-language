package cloud.kitelang.execution.struct;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.StructValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for struct declarations.
 */
@DisplayName("Struct Declaration")
public class StructDeclarationTest extends RuntimeTest {

    @Test
    @DisplayName("Block style struct declaration")
    void blockStyleDeclaration() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                Point
                """);

        assertInstanceOf(StructValue.class, result);
        var structValue = (StructValue) result;
        assertEquals("Point", structValue.getType());
    }

    @Test
    @DisplayName("Block style struct with defaults")
    void blockStyleWithDefaults() {
        var result = eval("""
                struct Point {
                    number x = 0
                    number y = 0
                }
                Point
                """);

        assertInstanceOf(StructValue.class, result);
        var structValue = (StructValue) result;
        assertEquals(0, structValue.get("x"));
        assertEquals(0, structValue.get("y"));
    }

    @Test
    @DisplayName("Inline style struct declaration")
    void inlineStyleDeclaration() {
        var result = eval("""
                struct Point { number x, number y }
                Point
                """);

        assertInstanceOf(StructValue.class, result);
        var structValue = (StructValue) result;
        assertEquals("Point", structValue.getType());
    }

    @Test
    @DisplayName("Inline style struct with defaults")
    void inlineStyleWithDefaults() {
        var result = eval("""
                struct Point { number x = 0, number y = 10 }
                Point
                """);

        assertInstanceOf(StructValue.class, result);
        var structValue = (StructValue) result;
        assertEquals(0, structValue.get("x"));
        assertEquals(10, structValue.get("y"));
    }

    @Test
    @DisplayName("Struct with string property")
    void structWithStringProperty() {
        var result = eval("""
                struct User {
                    string name
                    string email = "default@example.com"
                }
                User
                """);

        assertInstanceOf(StructValue.class, result);
        var structValue = (StructValue) result;
        assertEquals("default@example.com", structValue.get("email"));
    }

    @Test
    @DisplayName("Struct with boolean property")
    void structWithBooleanProperty() {
        var result = eval("""
                struct Config {
                    boolean enabled = true
                    boolean debug = false
                }
                Config
                """);

        assertInstanceOf(StructValue.class, result);
        var structValue = (StructValue) result;
        assertEquals(true, structValue.get("enabled"));
        assertEquals(false, structValue.get("debug"));
    }

    @Test
    @DisplayName("Struct is registered in environment")
    void structRegisteredInEnvironment() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                """);

        var point = interpreter.getVar("Point");
        assertNotNull(point);
        assertInstanceOf(StructValue.class, point);
    }
}
