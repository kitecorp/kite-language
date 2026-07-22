package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Execution-level tests (interpreter only, no TypeChecker pass) for map-typed var declarations.
 * These exist specifically to prove the Interpreter does not crash trying to resolve "map" as a
 * runtime variable name - unlike array element types (looked up to support union-type-alias Set
 * validation), a map's key/value types are never a registered runtime binding.
 */
@DisplayName("Interpreter var map")
public class VarMapTest extends RuntimeTest {

    @Test
    void testVarEmptyMap() {
        eval("""
                var map<string, number> x = {}
                """);
        Assertions.assertTrue(interpreter.hasVar("x"));
        var x = interpreter.getVar("x");
        assertEquals(Map.of(), x);
    }

    @Test
    void testVarMapWithValues() {
        eval("""
                var map<string, number> x = { a: 1, b: 2 }
                """);
        var x = interpreter.getVar("x");
        assertEquals(Map.of("a", 1, "b", 2), x);
    }

    @Test
    @DisplayName("map value type may be a nested map: map<string, map<string, number>>")
    void testNestedMap() {
        eval("""
                var map<string, map<string, number>> x = { a: { b: 1 } }
                """);
        var x = interpreter.getVar("x");
        assertEquals(Map.of("a", Map.of("b", 1)), x);
    }

    @Test
    @DisplayName("map used as a schema property resolves to the assigned runtime value on the resource")
    void testSchemaWithMapProperty() {
        eval("""
                schema Config {
                   map<string, number> counts
                }

                resource Config settings {
                   counts = { a: 1, b: 2 }
                }
                """);
        var settings = interpreter.getInstance("settings");
        assertEquals(Map.of("a", 1, "b", 2), settings.get("counts"));
    }
}
