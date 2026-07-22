package cloud.kitelang.semantics.typechecker;

import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.semantics.types.MapType;
import cloud.kitelang.semantics.types.SchemaType;
import cloud.kitelang.semantics.types.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TypeChecker var map")
public class VarMapTest extends CheckerTest {

    @Test
    void testVarEmptyMap() {
        eval("""
                var map<string, number> x = {}
                """);
        var varType = checker.getEnv().lookup("x");
        Assertions.assertInstanceOf(MapType.class, varType);
    }

    @Test
    void testKeyAndValueTypeResolved() {
        eval("""
                var map<string, number> x = {}
                """);
        var varType = (MapType) checker.getEnv().lookup("x");
        assertEquals(ValueType.String, varType.getKeyType());
        assertEquals(ValueType.Number, varType.getValueType());
    }

    @Test
    void testStringValueMap() {
        eval("""
                var map<string, string> x = { a: "hi" }
                """);
        var varType = (MapType) checker.getEnv().lookup("x");
        assertEquals(ValueType.String, varType.getValueType());
    }

    @Test
    void testValidValueInit() {
        eval("""
                var map<string, number> x = { a: 1, b: 2 }
                """);
        var varType = (MapType) checker.getEnv().lookup("x");
        assertEquals(ValueType.Number, varType.getValueType());
    }

    @Test
    void testWrongValueTypeThrows() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var map<string, number> x = { a: "not a number" }
                """));
    }

    @Test
    void testNonObjectInitThrows() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                var map<string, number> x = 1
                """));
    }

    @Test
    @DisplayName("nested maps type-check: map<string, map<string, number>>")
    void testNestedMap() {
        eval("""
                var map<string, map<string, number>> x = { a: { b: 1 } }
                """);
        var varType = (MapType) checker.getEnv().lookup("x");
        Assertions.assertInstanceOf(MapType.class, varType.getValueType());
        var nested = (MapType) varType.getValueType();
        assertEquals(ValueType.Number, nested.getValueType());
    }

    @Test
    @DisplayName("map used as a schema property type")
    void testSchemaPropertyMapType() {
        var actual = checker.visit(parse("""
                schema Config {
                   map<string, number> counts
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        var schema = (SchemaType) actual;
        var propertyType = schema.getEnvironment().lookup("counts");
        Assertions.assertInstanceOf(MapType.class, propertyType);
        var mapType = (MapType) propertyType;
        assertEquals(ValueType.String, mapType.getKeyType());
        assertEquals(ValueType.Number, mapType.getValueType());
    }

    @Test
    @DisplayName("map value type may be a nested schema type")
    void testSchemaPropertyMapOfSchemaType() {
        var actual = checker.visit(parse("""
                schema Address {
                    string city
                }

                schema Config {
                   map<string, Address> addresses
                }
                """));

        var config = (SchemaType) actual;
        var propertyType = config.getEnvironment().lookup("addresses");
        Assertions.assertInstanceOf(MapType.class, propertyType);
        var mapType = (MapType) propertyType;
        assertEquals("Address", mapType.getValueType().name());
    }
}
