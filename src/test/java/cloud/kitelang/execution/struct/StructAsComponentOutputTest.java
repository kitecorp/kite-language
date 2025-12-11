package cloud.kitelang.execution.struct;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.StructValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for struct as component output.
 */
@DisplayName("Struct as Component Output")
public class StructAsComponentOutputTest extends RuntimeTest {

    @Test
    @DisplayName("Component can have struct type output")
    void componentWithStructOutput() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                component Shape {
                    output Point center = Point(50, 50)
                }
                component Shape myShape {
                }
                """);

        var instance = interpreter.getComponent("myShape");
        var center = instance.argVal("center");
        assertInstanceOf(StructValue.class, center);
        assertEquals(50, ((StructValue) center).get("x"));
        assertEquals(50, ((StructValue) center).get("y"));
    }

    @Test
    @DisplayName("Access struct output from outside component")
    void accessStructOutputFromOutside() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                component Shape {
                    output Point center = Point(50, 50)
                }
                component Shape myShape {
                }
                var c = myShape.center
                """);

        var c = interpreter.getVar("c");
        assertInstanceOf(StructValue.class, c);
        assertEquals(50, ((StructValue) c).get("x"));
    }

    @Test
    @DisplayName("Component output computed from input")
    void componentOutputComputedFromInput() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                component Shape {
                    input number width = 100
                    input number height = 100
                    output Point center = Point(width / 2, height / 2)
                }
                component Shape myShape {
                    width = 200
                    height = 100
                }
                """);

        var instance = interpreter.getComponent("myShape");
        var center = (StructValue) instance.argVal("center");
        assertEquals(100, center.get("x"));
        assertEquals(50, center.get("y"));
    }

    @Test
    @DisplayName("Struct array as component output")
    void structArrayAsComponentOutput() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                component Shape {
                    output Point[] corners = [Point(0, 0), Point(100, 0), Point(100, 100), Point(0, 100)]
                }
                component Shape myShape {
                }
                """);

        var instance = interpreter.getComponent("myShape");
        var corners = (java.util.List<?>) instance.argVal("corners");
        assertEquals(4, corners.size());
        assertEquals(100, ((StructValue) corners.get(1)).get("x"));
    }

    @Test
    @DisplayName("Component with both struct input and output")
    void componentWithStructInputAndOutput() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                component Shape {
                    input Point origin
                    output Point center = Point(origin.x + 50, origin.y + 50)
                }
                component Shape myShape {
                    origin = Point(10, 20)
                }
                """);

        var instance = interpreter.getComponent("myShape");
        var origin = (StructValue) instance.argVal("origin");
        var center = (StructValue) instance.argVal("center");

        assertEquals(10, origin.get("x"));
        assertEquals(60, center.get("x"));
        assertEquals(70, center.get("y"));
    }

    @Test
    @DisplayName("One component uses another component struct output")
    void componentUsesAnotherComponentStructOutput() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                component Shape {
                    input Point origin = Point(0, 0)
                    output Point center = Point(origin.x + 50, origin.y + 50)
                }
                component Shape shapeA {
                    origin = Point(0, 0)
                }
                component Shape shapeB {
                    origin = shapeA.center
                }
                """);

        var shapeB = interpreter.getComponent("shapeB");
        var origin = (StructValue) shapeB.argVal("origin");
        assertEquals(50, origin.get("x"));
        assertEquals(50, origin.get("y"));
    }
}
