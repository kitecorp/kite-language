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

    @Test
    @DisplayName("Output depends on two overridden inputs")
    void outputDependsOnTwoOverriddenInputs() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                component Box {
                    input number width = 10
                    input number height = 10
                    output Point diagonal = Point(width, height)
                }
                component Box myBox {
                    width = 300
                    height = 400
                }
                """);

        var instance = interpreter.getComponent("myBox");
        var diagonal = (StructValue) instance.argVal("diagonal");
        assertEquals(300, diagonal.get("x"));
        assertEquals(400, diagonal.get("y"));
    }

    @Test
    @DisplayName("Multiple outputs each depend on different overridden inputs")
    void multipleOutputsDependOnDifferentInputs() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                component Rect {
                    input number left = 0
                    input number top = 0
                    input number right = 100
                    input number bottom = 100
                    output Point topLeft = Point(left, top)
                    output Point bottomRight = Point(right, bottom)
                }
                component Rect myRect {
                    left = 10
                    top = 20
                    right = 110
                    bottom = 120
                }
                """);

        var instance = interpreter.getComponent("myRect");
        var topLeft = (StructValue) instance.argVal("topLeft");
        var bottomRight = (StructValue) instance.argVal("bottomRight");
        assertEquals(10, topLeft.get("x"));
        assertEquals(20, topLeft.get("y"));
        assertEquals(110, bottomRight.get("x"));
        assertEquals(120, bottomRight.get("y"));
    }

    @Test
    @DisplayName("Output uses string interpolation with overridden input")
    void outputUsesStringInterpolationWithOverriddenInput() {
        eval("""
                component Server {
                    input string name = "default"
                    input number port = 8080
                    output string url = "http://$name:$port"
                }
                component Server api {
                    name = "api-server"
                    port = 3000
                }
                """);

        var instance = interpreter.getComponent("api");
        var url = instance.argVal("url");
        assertEquals("http://api-server:3000", url);
    }

    @Test
    @DisplayName("Output uses arithmetic expression on overridden input")
    void outputUsesArithmeticOnOverriddenInput() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                component Circle {
                    input number radius = 50
                    output number diameter = radius * 2
                    output Point top = Point(0, -radius)
                }
                component Circle myCircle {
                    radius = 100
                }
                """);

        var instance = interpreter.getComponent("myCircle");
        assertEquals(200, instance.argVal("diameter"));
        var top = (StructValue) instance.argVal("top");
        assertEquals(0, top.get("x"));
        assertEquals(-100, top.get("y"));
    }

    @Test
    @DisplayName("Output with default input values not overridden")
    void outputWithDefaultInputNotOverridden() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                component Shape {
                    input number width = 100
                    input number height = 200
                    output Point center = Point(width / 2, height / 2)
                }
                component Shape myShape {
                    width = 400
                }
                """);

        var instance = interpreter.getComponent("myShape");
        var center = (StructValue) instance.argVal("center");
        // width overridden to 400, height stays at default 200
        assertEquals(200, center.get("x"));
        assertEquals(100, center.get("y"));
    }
}
