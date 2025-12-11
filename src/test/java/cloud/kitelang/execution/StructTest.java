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

    @Nested
    @DisplayName("Struct as Schema Field")
    class StructAsSchemaFieldTests {

        @Test
        @DisplayName("Schema can have struct type field")
        void schemaWithStructField() {
            eval("""
                    struct Point {
                        number x
                        number y
                    }
                    schema Shape {
                        Point origin
                    }
                    """);

            var schema = interpreter.getSchema("Shape");
            assertTrue(schema.has("origin"));
        }

        @Test
        @DisplayName("Schema can have struct field with default")
        void schemaWithStructFieldDefault() {
            eval("""
                    struct Point {
                        number x = 0
                        number y = 0
                    }
                    schema Shape {
                        Point origin = Point()
                    }
                    """);

            var schema = interpreter.getSchema("Shape");
            assertTrue(schema.has("origin"));
            var origin = schema.get("origin");
            assertInstanceOf(StructValue.class, origin);
            assertEquals(0, ((StructValue) origin).get("x"));
        }

        @Test
        @DisplayName("Schema with multiple struct fields")
        void schemaWithMultipleStructFields() {
            eval("""
                    struct Point {
                        number x
                        number y
                    }
                    struct Size {
                        number width
                        number height
                    }
                    schema Rectangle {
                        Point topLeft
                        Size dimensions
                    }
                    """);

            var schema = interpreter.getSchema("Rectangle");
            assertTrue(schema.has("topLeft"));
            assertTrue(schema.has("dimensions"));
        }
    }

    @Nested
    @DisplayName("Struct in Resource Creation")
    class StructInResourceCreationTests {

        @Test
        @DisplayName("Set struct value using constructor in resource")
        void setStructUsingConstructor() {
            eval("""
                    struct Point {
                        number x
                        number y
                    }
                    schema Shape {
                        Point origin
                    }
                    resource Shape myShape {
                        origin = Point(10, 20)
                    }
                    """);

            var resource = interpreter.getInstance("myShape");
            var origin = resource.argVal("origin");
            assertInstanceOf(StructValue.class, origin);
            assertEquals(10, ((StructValue) origin).get("x"));
            assertEquals(20, ((StructValue) origin).get("y"));
        }

        @Test
        @DisplayName("Set struct value using object literal in resource")
        void setStructUsingObjectLiteral() {
            eval("""
                    struct Point {
                        number x
                        number y
                    }
                    schema Shape {
                        Point origin
                    }
                    resource Shape myShape {
                        origin = { x: 30, y: 40 }
                    }
                    """);

            var resource = interpreter.getInstance("myShape");
            var origin = resource.argVal("origin");
            // Object literal should be auto-coerced to struct when schema field has struct type
            assertNotNull(origin);
        }

        @Test
        @DisplayName("Access struct property from resource")
        void accessStructPropertyFromResource() {
            eval("""
                    struct Point {
                        number x
                        number y
                    }
                    schema Shape {
                        Point origin
                    }
                    resource Shape myShape {
                        origin = Point(10, 20)
                    }
                    """);

            var resource = interpreter.getInstance("myShape");
            var origin = (StructValue) resource.argVal("origin");
            assertEquals(10, origin.get("x"));
            assertEquals(20, origin.get("y"));
        }

        @Test
        @DisplayName("Resource inherits struct default from schema")
        void resourceInheritsStructDefault() {
            eval("""
                    struct Point {
                        number x = 0
                        number y = 0
                    }
                    schema Shape {
                        Point origin = Point(5, 5)
                    }
                    resource Shape myShape {
                    }
                    """);

            var resource = interpreter.getInstance("myShape");
            var origin = resource.argVal("origin");
            assertInstanceOf(StructValue.class, origin);
            assertEquals(5, ((StructValue) origin).get("x"));
            assertEquals(5, ((StructValue) origin).get("y"));
        }

        @Test
        @DisplayName("Resource overrides struct default from schema")
        void resourceOverridesStructDefault() {
            eval("""
                    struct Point {
                        number x = 0
                        number y = 0
                    }
                    schema Shape {
                        Point origin = Point(5, 5)
                    }
                    resource Shape myShape {
                        origin = Point(100, 200)
                    }
                    """);

            var resource = interpreter.getInstance("myShape");
            var origin = resource.argVal("origin");
            assertInstanceOf(StructValue.class, origin);
            assertEquals(100, ((StructValue) origin).get("x"));
            assertEquals(200, ((StructValue) origin).get("y"));
        }

        @Test
        @DisplayName("Struct with partial defaults in resource")
        void structWithPartialDefaultsInResource() {
            eval("""
                    struct Config {
                        string host
                        number port = 8080
                    }
                    schema Server {
                        Config config
                    }
                    resource Server myServer {
                        config = Config("localhost")
                    }
                    """);

            var resource = interpreter.getInstance("myServer");
            var config = resource.argVal("config");
            assertInstanceOf(StructValue.class, config);
            assertEquals("localhost", ((StructValue) config).get("host"));
            assertEquals(8080, ((StructValue) config).get("port"));
        }

        @Test
        @DisplayName("Resource references another resource struct")
        void resourceReferencesAnotherResourceStruct() {
            eval("""
                    struct Point {
                        number x
                        number y
                    }
                    schema Shape {
                        Point origin
                        Point refPoint
                    }
                    resource Shape shapeA {
                        origin = Point(10, 20)
                    }
                    resource Shape shapeB {
                        origin = Point(0, 0)
                        refPoint = shapeA.origin
                    }
                    """);

            var shapeB = interpreter.getInstance("shapeB");
            var refPoint = (StructValue) shapeB.argVal("refPoint");
            assertEquals(10, refPoint.get("x"));
            assertEquals(20, refPoint.get("y"));
        }

        @Test
        @DisplayName("Nested struct in resource")
        void nestedStructInResource() {
            eval("""
                    struct Point {
                        number x
                        number y
                    }
                    struct Rect {
                        Point topLeft
                        Point bottomRight
                    }
                    schema Canvas {
                        Rect bounds
                    }
                    resource Canvas myCanvas {
                        bounds = Rect(Point(0, 0), Point(100, 100))
                    }
                    """);

            var resource = interpreter.getInstance("myCanvas");
            var bounds = resource.argVal("bounds");
            assertInstanceOf(StructValue.class, bounds);

            var topLeft = ((StructValue) bounds).get("topLeft");
            assertInstanceOf(StructValue.class, topLeft);
            assertEquals(0, ((StructValue) topLeft).get("x"));
        }

        @Test
        @DisplayName("Multiple resources with same struct schema field")
        void multipleResourcesWithSameStructField() {
            eval("""
                    struct Point {
                        number x
                        number y
                    }
                    schema Shape {
                        Point origin
                    }
                    resource Shape circle {
                        origin = Point(10, 10)
                    }
                    resource Shape square {
                        origin = Point(20, 20)
                    }
                    """);

            var circle = interpreter.getInstance("circle");
            var square = interpreter.getInstance("square");

            var circleOrigin = (StructValue) circle.argVal("origin");
            var squareOrigin = (StructValue) square.argVal("origin");

            assertEquals(10, circleOrigin.get("x"));
            assertEquals(20, squareOrigin.get("x"));
        }

        @Test
        @DisplayName("Struct array in resource")
        void structArrayInResource() {
            eval("""
                    struct Point {
                        number x
                        number y
                    }
                    schema Path {
                        Point[] points
                    }
                    resource Path myPath {
                        points = [Point(0, 0), Point(10, 10), Point(20, 20)]
                    }
                    """);

            var resource = interpreter.getInstance("myPath");
            var points = (java.util.List<?>) resource.argVal("points");
            assertEquals(3, points.size());
            assertInstanceOf(StructValue.class, points.get(0));
            assertEquals(10, ((StructValue) points.get(1)).get("x"));
        }

        @Test
        @DisplayName("Computed struct value in resource")
        void computedStructValueInResource() {
            eval("""
                    struct Point {
                        number x
                        number y
                    }
                    schema Shape {
                        Point origin
                    }
                    var xVal = 5 * 2
                    var yVal = 10 + 5
                    resource Shape myShape {
                        origin = Point(xVal, yVal)
                    }
                    """);

            var resource = interpreter.getInstance("myShape");
            var origin = (StructValue) resource.argVal("origin");
            assertEquals(10, origin.get("x"));
            assertEquals(15, origin.get("y"));
        }
    }
}
