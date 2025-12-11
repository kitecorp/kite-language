package cloud.kitelang.execution.struct;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.StructValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for standalone input with struct type.
 */
@DisplayName("Standalone Input with Struct")
public class StandaloneInputStructTest extends RuntimeTest {

    @Test
    @DisplayName("Standalone input with struct type and default value")
    void standaloneInputStructWithDefault() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                input Point origin = Point(10, 20)
                """);

        assertInstanceOf(StructValue.class, result);
        assertEquals(10, ((StructValue) result).get("x"));
        assertEquals(20, ((StructValue) result).get("y"));
    }

    @Test
    @DisplayName("Standalone input struct used in expression")
    void standaloneInputStructUsedInExpression() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                input Point origin = Point(10, 20)
                var sum = origin.x + origin.y
                sum
                """);

        assertEquals(30, result);
    }

    @Test
    @DisplayName("Standalone input struct array with default")
    void standaloneInputStructArrayWithDefault() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                input Point[] points = [Point(0, 0), Point(10, 10)]
                """);

        assertInstanceOf(java.util.List.class, result);
        var points = (java.util.List<?>) result;
        assertEquals(2, points.size());
        assertEquals(10, ((StructValue) points.get(1)).get("x"));
    }

    @Test
    @DisplayName("Standalone input with nested struct default")
    void standaloneInputNestedStructWithDefault() {
        var result = eval("""
                struct Point {
                    number x
                    number y
                }
                struct Rect {
                    Point topLeft
                    Point bottomRight
                }
                input Rect bounds = Rect(Point(0, 0), Point(100, 100))
                """);

        assertInstanceOf(StructValue.class, result);
        var bounds = (StructValue) result;
        var topLeft = (StructValue) bounds.get("topLeft");
        assertEquals(0, topLeft.get("x"));
    }

    @Test
    @DisplayName("Standalone input struct used in resource")
    void standaloneInputStructUsedInResource() {
        eval("""
                struct Point {
                    number x
                    number y
                }
                schema Shape {
                    Point origin
                }
                input Point defaultOrigin = Point(25, 25)
                resource Shape myShape {
                    origin = defaultOrigin
                }
                """);

        var resource = interpreter.getInstance("myShape");
        var origin = (StructValue) resource.argVal("origin");
        assertEquals(25, origin.get("x"));
    }
}
