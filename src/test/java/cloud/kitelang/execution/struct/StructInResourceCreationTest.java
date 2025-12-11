package cloud.kitelang.execution.struct;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.StructValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for struct in resource creation.
 */
@DisplayName("Struct in Resource Creation")
public class StructInResourceCreationTest extends RuntimeTest {

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
