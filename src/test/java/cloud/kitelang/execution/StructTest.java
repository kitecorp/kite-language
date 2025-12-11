package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.StructValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for struct declarations and usage.
 */
public class StructTest extends RuntimeTest {

    @Nested
    @DisplayName("Struct Declaration")
    class StructDeclarationTests {

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

            var point = interpreter.getEnv().lookup("Point");
            assertNotNull(point);
            assertInstanceOf(StructValue.class, point);
        }
    }

    @Nested
    @DisplayName("Struct with Annotations")
    class StructAnnotationTests {

        @Test
        @DisplayName("Struct property with @cloud annotation")
        void structWithCloudAnnotation() {
            var result = eval("""
                    struct AWSResource {
                        string name
                        @cloud string arn
                    }
                    AWSResource
                    """);

            assertInstanceOf(StructValue.class, result);
            var structValue = (StructValue) result;
            assertTrue(structValue.isCloudProperty("arn"));
            assertFalse(structValue.isCloudProperty("name"));
        }

        @Test
        @DisplayName("Cloud property cannot have initialization")
        void cloudPropertyCannotHaveInit() {
            assertThrows(Exception.class, () -> eval("""
                    struct AWSResource {
                        string name
                        @cloud string arn = "arn:aws:..."
                    }
                    """));
        }
    }

    @Nested
    @DisplayName("Struct Instantiation")
    class StructInstantiationTests {

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

    @Nested
    @DisplayName("Struct Auto-Coercion")
    class StructAutoCoercionTests {

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
}
