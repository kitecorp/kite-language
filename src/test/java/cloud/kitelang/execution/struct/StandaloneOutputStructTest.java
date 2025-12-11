package cloud.kitelang.execution.struct;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.StructValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for standalone output with struct type.
 */
@DisplayName("Standalone Output with Struct")
public class StandaloneOutputStructTest extends RuntimeTest {

    @Test
    @DisplayName("Standalone output with struct type")
    void standaloneOutputStruct() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                output Point origin = Point(10, 20)
                """);

        assertInstanceOf(StructValue.class, result);
        assertEquals(10, ((StructValue) result).get("x"));
        assertEquals(20, ((StructValue) result).get("y"));
    }

    @Test
    @DisplayName("Standalone output struct from resource property")
    void standaloneOutputStructFromResource() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                schema Shape {
                    Point origin
                }
                resource Shape myShape {
                    origin = Point(50, 60)
                }
                output Point shapeOrigin = myShape.origin
                """);

        assertInstanceOf(StructValue.class, result);
        assertEquals(50, ((StructValue) result).get("x"));
    }

    @Test
    @DisplayName("Standalone output struct array")
    void standaloneOutputStructArray() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                output Point[] corners = [Point(0, 0), Point(100, 0), Point(100, 100), Point(0, 100)]
                """);

        assertInstanceOf(java.util.List.class, result);
        var corners = (java.util.List<?>) result;
        assertEquals(4, corners.size());
        assertEquals(100, ((StructValue) corners.get(2)).get("x"));
    }

    @Test
    @DisplayName("Standalone output with nested struct")
    void standaloneOutputNestedStruct() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                struct Rect {
                    Point topLeft
                    Point bottomRight
                }
                output Rect bounds = Rect(Point(0, 0), Point(100, 100))
                """);

        assertInstanceOf(StructValue.class, result);
        var bounds = (StructValue) result;
        var bottomRight = (StructValue) bounds.get("bottomRight");
        assertEquals(100, bottomRight.get("x"));
    }

    @Test
    @DisplayName("Standalone output struct from component output")
    void standaloneOutputStructFromComponent() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                component Shape {
                    output Point center = Point(50, 50)
                }
                component Shape myShape {
                }
                output Point shapeCenter = myShape.center
                """);

        assertInstanceOf(StructValue.class, result);
        assertEquals(50, ((StructValue) result).get("x"));
    }

    @Test
    @DisplayName("Multiple standalone outputs with struct type")
    void multipleStandaloneOutputsWithStruct() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                output Point origin = Point(0, 0)
                output Point center = Point(50, 50)
                output Point corner = Point(100, 100)
                """);

        var outputs = interpreter.getOutputs();
        assertEquals(3, outputs.size());

        // Outputs are stored as declarations, values are in env
        var origin = (StructValue) interpreter.getEnv().lookup("origin");
        var center = (StructValue) interpreter.getEnv().lookup("center");
        var corner = (StructValue) interpreter.getEnv().lookup("corner");

        assertEquals(0, origin.get("x"));
        assertEquals(50, center.get("x"));
        assertEquals(100, corner.get("x"));
    }
}
