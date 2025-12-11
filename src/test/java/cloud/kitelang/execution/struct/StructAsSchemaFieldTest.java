package cloud.kitelang.execution.struct;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.StructValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for struct as schema field.
 */
@DisplayName("Struct as Schema Field")
public class StructAsSchemaFieldTest extends RuntimeTest {

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
