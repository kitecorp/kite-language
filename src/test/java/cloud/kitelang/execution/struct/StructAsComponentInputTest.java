package cloud.kitelang.execution.struct;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.StructValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for struct as component input.
 */
@DisplayName("Struct as Component Input")
public class StructAsComponentInputTest extends RuntimeTest {

    @Test
    @DisplayName("Component can have struct type input")
    void componentWithStructInput() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                component Shape {
                    input Point origin
                }
                component Shape myShape {
                    origin = Point(10, 20)
                }
                """);

        var instance = interpreter.getComponent("myShape");
        var origin = instance.argVal("origin");
        assertInstanceOf(StructValue.class, origin);
        assertEquals(10, ((StructValue) origin).get("x"));
        assertEquals(20, ((StructValue) origin).get("y"));
    }

    @Test
    @DisplayName("Component can have struct input with default")
    void componentWithStructInputDefault() {
        eval("""
                struct Point {
                    number x = 0
                    number y = 0
                }
                component Shape {
                    input Point origin = Point(5, 5)
                }
                component Shape myShape {
                }
                """);

        var instance = interpreter.getComponent("myShape");
        var origin = instance.argVal("origin");
        assertInstanceOf(StructValue.class, origin);
        assertEquals(5, ((StructValue) origin).get("x"));
        assertEquals(5, ((StructValue) origin).get("y"));
    }

    @Test
    @DisplayName("Component instance overrides struct input default")
    void componentInstanceOverridesStructInputDefault() {
        eval("""
                struct Point {
                    number x = 0
                    number y = 0
                }
                component Shape {
                    input Point origin = Point(5, 5)
                }
                component Shape myShape {
                    origin = Point(100, 200)
                }
                """);

        var instance = interpreter.getComponent("myShape");
        var origin = (StructValue) instance.argVal("origin");
        assertEquals(100, origin.get("x"));
        assertEquals(200, origin.get("y"));
    }

    @Test
    @DisplayName("Multiple struct inputs in component")
    void multipleStructInputsInComponent() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                struct Size {
                    number width
                    number height
                }
                component Rectangle {
                    input Point topLeft
                    input Size dimensions
                }
                component Rectangle myRect {
                    topLeft = Point(0, 0)
                    dimensions = Size(100, 50)
                }
                """);

        var instance = interpreter.getComponent("myRect");
        var topLeft = (StructValue) instance.argVal("topLeft");
        var dimensions = (StructValue) instance.argVal("dimensions");

        assertEquals(0, topLeft.get("x"));
        assertEquals(100, dimensions.get("width"));
    }

    @Test
    @DisplayName("Struct input with partial defaults")
    void structInputWithPartialDefaults() {
        eval("""
                struct Config {
                    string host
                    number port = 8080
                }
                component Server {
                    input Config config
                }
                component Server myServer {
                    config = Config("localhost")
                }
                """);

        var instance = interpreter.getComponent("myServer");
        var config = (StructValue) instance.argVal("config");
        assertEquals("localhost", config.get("host"));
        assertEquals(8080, config.get("port"));
    }

    @Test
    @DisplayName("Access struct input from outside component")
    void accessStructInputFromOutside() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                component Shape {
                    input Point origin
                }
                component Shape myShape {
                    origin = Point(10, 20)
                }
                var p = myShape.origin
                """);

        var p = interpreter.getVar("p");
        assertInstanceOf(StructValue.class, p);
        assertEquals(10, ((StructValue) p).get("x"));
    }

    @Test
    @DisplayName("Component references another component struct input")
    void componentReferencesAnotherComponentStructInput() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                component Shape {
                    input Point origin
                    input Point refPoint
                }
                component Shape shapeA {
                    origin = Point(10, 20)
                }
                component Shape shapeB {
                    origin = Point(0, 0)
                    refPoint = shapeA.origin
                }
                """);

        var shapeB = interpreter.getComponent("shapeB");
        var refPoint = (StructValue) shapeB.argVal("refPoint");
        assertEquals(10, refPoint.get("x"));
        assertEquals(20, refPoint.get("y"));
    }

    @Test
    @DisplayName("Struct array as component input")
    void structArrayAsComponentInput() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                component Path {
                    input Point[] points
                }
                component Path myPath {
                    points = [Point(0, 0), Point(10, 10), Point(20, 20)]
                }
                """);

        var instance = interpreter.getComponent("myPath");
        var points = (java.util.List<?>) instance.argVal("points");
        assertEquals(3, points.size());
        assertInstanceOf(StructValue.class, points.get(0));
        assertEquals(10, ((StructValue) points.get(1)).get("x"));
    }

    @Test
    @DisplayName("Nested struct as component input")
    void nestedStructAsComponentInput() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                struct Rect {
                    Point topLeft
                    Point bottomRight
                }
                component Canvas {
                    input Rect bounds
                }
                component Canvas myCanvas {
                    bounds = Rect(Point(0, 0), Point(100, 100))
                }
                """);

        var instance = interpreter.getComponent("myCanvas");
        var bounds = (StructValue) instance.argVal("bounds");
        var topLeft = (StructValue) bounds.get("topLeft");
        assertEquals(0, topLeft.get("x"));
    }
}
