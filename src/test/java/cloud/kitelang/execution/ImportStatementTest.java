package cloud.kitelang.execution;

import cloud.kitelang.analysis.ImportResolver;
import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Interpreter Import Statement")
class ImportStatementTest extends RuntimeTest {

    @BeforeEach
    void clearImportCache() {
        ImportResolver.clearCache();
    }

    @Test
    void importStatementBasic() {
        var res = eval("""
                import * from "stdlib.kite"

                var result = double(5)
                """);

        assertNotNull(res);
        // Verify the imported function was called successfully (5 * 2 = 10 as integer)
        assertEquals(10, interpreter.getVar("result"));
    }

    @Test
    void importStatementAccessVariable() {
        eval("""
                import * from "stdlib.kite"

                var msg = greeting
                """);
        assertEquals("Hello from stdlib!", interpreter.getVar("msg"));
    }

    @Test
    void importStatementCallFunction() {
        eval("""
                import * from "stdlib.kite"

                var doubled = double(10)
                var tripled = triple(5)
                """);
        assertEquals(20, interpreter.getVar("doubled"));  // 10 * 2 = 20 (integer)
        assertEquals(15, interpreter.getVar("tripled"));  // 5 * 3 = 15 (integer)
    }

    @Test
    void importStatementNonExistentFile() {
        var code = """
                import * from "nonexistent.kite"
                """;
        assertThrows(RuntimeException.class, () -> eval(code));
    }

    @Test
    void importStatementNested() {
        // Test nested imports: main -> nested_b -> nested_a -> stdlib
        var code = """
                import * from "nested_b.kite"

                var result = valueB
                """;
        eval(code);

        // valueB should be 10 (from nested_b.kite)
        // which uses valueA (6 from nested_a.kite, which is double(3) from stdlib)
        assertEquals(10, interpreter.getVar("result"));
    }

    @Test
    void importStatementCircular() {
        // Test circular import detection: circular_a imports circular_b, which imports circular_a
        var code = """
                import * from "circular_a.kite"
                """;

        RuntimeException exception = assertThrows(RuntimeException.class, () -> eval(code));
        assertTrue(exception.getMessage().contains("Circular import detected"),
                exception.getMessage());
        // Verify the chain includes both files (path-agnostic check)
        assertTrue(exception.getMessage().contains("circular_a.kite") &&
                   exception.getMessage().contains("circular_b.kite"),
                "Expected circular import chain with both files: " + exception.getMessage());
    }

    // ========== Complex Import Tests ==========

    @Test
    @DisplayName("should cache parsed programs to avoid re-parsing")
    void shouldCacheParsedPrograms() {
        assertEquals(0, ImportResolver.getCacheSize(), "Cache should be empty initially");

        eval("""
                import * from "stdlib.kite"
                """);

        assertTrue(ImportResolver.getCacheSize() > 0, "Cache should have entries after import");
        int cacheSize = ImportResolver.getCacheSize();

        // Import the same file again - cache size should not change
        eval("""
                import * from "stdlib.kite"
                """);

        assertEquals(cacheSize, ImportResolver.getCacheSize(), "Cache size should not increase for same file");
    }

    @Test
    @DisplayName("should handle multiple imports in same file")
    void multipleImportsInSameFile() {
        eval("""
                import * from "imports/math_utils.kite"
                import * from "imports/string_utils.kite"

                var mySum = add(1, 2)
                var myProduct = multiply(3, 4)
                var myMessage = greet("World")
                var myPi = PI
                var myGreeting = DEFAULT_GREETING
                """);

        // Math functions should work
        assertEquals(3, interpreter.getVar("mySum"));
        assertEquals(12, interpreter.getVar("myProduct"));

        // String functions should work
        assertEquals("Hello, World", interpreter.getVar("myMessage"));

        // Variables from both files should be available
        assertEquals(3.14159, interpreter.getVar("myPi"));
        assertEquals("Welcome", interpreter.getVar("myGreeting"));
    }

    @Test
    @DisplayName("should handle diamond dependency pattern")
    void diamondDependencyPattern() {
        eval("""
                import * from "imports/diamond_top.kite"

                var combined = COMBINED
                var result = process(5)
                """);

        // SHARED_VALUE = 42, LEFT_VALUE = 42 + 10 = 52, RIGHT_VALUE = 42 + 20 = 62
        // COMBINED = 52 + 62 = 114
        assertEquals(42, interpreter.getVar("SHARED_VALUE"));
        assertEquals(52, interpreter.getVar("LEFT_VALUE"));
        assertEquals(62, interpreter.getVar("RIGHT_VALUE"));
        assertEquals(114, interpreter.getVar("combined"));

        // process(5) = leftProcess(5) + rightProcess(5) = identity(5)*2 + identity(5)*3 = 10 + 15 = 25
        assertEquals(25, interpreter.getVar("result"));
    }

    @Test
    @DisplayName("should cache shared imports in diamond pattern")
    void diamondPatternUsesCache() {
        assertEquals(0, ImportResolver.getCacheSize(), "Cache should be empty initially");

        eval("""
                import * from "imports/diamond_top.kite"
                """);

        // Cache should contain: diamond_top, diamond_left, diamond_right, common
        // common.kite is imported by both left and right but should only be parsed once
        int cacheSize = ImportResolver.getCacheSize();
        assertEquals(4, cacheSize, "Cache should have exactly 4 entries (common parsed once)");
    }

    @Test
    @DisplayName("should allow importing same file multiple times explicitly")
    void importSameFileMultipleTimes() {
        // Importing the same file twice should work (idempotent)
        eval("""
                import * from "imports/math_utils.kite"
                import * from "imports/math_utils.kite"

                var result = add(1, multiply(2, 3))
                """);

        // 1 + (2 * 3) = 1 + 6 = 7
        assertEquals(7, interpreter.getVar("result"));
    }

    @Test
    @DisplayName("should execute functions from multiple imports together")
    void executeFunctionsFromMultipleImports() {
        eval("""
                import * from "imports/math_utils.kite"
                import * from "imports/string_utils.kite"

                var myNum = add(10, 20)
                var myMsg = greet("Kite")
                var myCombined = myMsg + " - result"
                """);

        assertEquals(30, interpreter.getVar("myNum"));
        assertEquals("Hello, Kite", interpreter.getVar("myMsg"));
        assertEquals("Hello, Kite - result", interpreter.getVar("myCombined"));
    }

    @Test
    @DisplayName("should handle deeply nested function calls across imports")
    void deeplyNestedFunctionCalls() {
        eval("""
                import * from "imports/math_utils.kite"

                var result = add(multiply(2, 3), multiply(4, 5))
                """);

        // (2 * 3) + (4 * 5) = 6 + 20 = 26
        assertEquals(26, interpreter.getVar("result"));
    }

    @Test
    @DisplayName("should use imported variables in expressions")
    void useImportedVariablesInExpressions() {
        eval("""
                import * from "imports/math_utils.kite"

                var circumference = 2 * PI * 10
                """);

        // 2 * 3.14159 * 10 = 62.8318
        var result = (Number) interpreter.getVar("circumference");
        assertEquals(62.8318, result.doubleValue(), 0.001);
    }

    @Test
    @DisplayName("should handle import chain with value propagation")
    void importChainWithValuePropagation() {
        // nested_b imports nested_a which imports stdlib
        // This tests that values computed in nested imports are correctly propagated
        eval("""
                import * from "nested_b.kite"

                var a = valueA
                var b = valueB
                """);

        // valueA = double(3) = 6 (from nested_a)
        // valueB = valueA + 4 = 10 (from nested_b)
        assertEquals(6, interpreter.getVar("a"));
        assertEquals(10, interpreter.getVar("b"));
    }

    // ========== Named Import Tests ==========

    @Test
    @DisplayName("should import only specified symbol with named import")
    void namedImportSingleSymbol() {
        eval("""
                import add from "imports/math_utils.kite"

                var result = add(1, 2)
                """);

        // add should work
        assertEquals(3, interpreter.getVar("result"));

        // multiply and PI should NOT be available
        assertFalse(interpreter.hasVar("multiply"), "multiply should NOT be imported");
        assertFalse(interpreter.hasVar("PI"), "PI should NOT be imported");
    }

    @Test
    @DisplayName("should import multiple specified symbols with named import")
    void namedImportMultipleSymbols() {
        eval("""
                import add, PI from "imports/math_utils.kite"

                var result = add(1, 2)
                var myPi = PI
                """);

        // add and PI should work
        assertEquals(3, interpreter.getVar("result"));
        assertEquals(3.14159, interpreter.getVar("myPi"));

        // multiply should NOT be available
        assertFalse(interpreter.hasVar("multiply"), "multiply should NOT be imported");
    }

    @Test
    @DisplayName("should error when importing non-existent symbol")
    void namedImportNonExistentSymbol() {
        var exception = assertThrows(RuntimeException.class, () -> eval("""
                import nonExistent from "imports/math_utils.kite"
                """));

        assertTrue(exception.getMessage().contains("nonExistent") ||
                   exception.getMessage().contains("not found"),
                "Error should mention missing symbol: " + exception.getMessage());
    }

    @Test
    @DisplayName("should mix named import with wildcard import")
    void mixNamedAndWildcardImports() {
        eval("""
                import add from "imports/math_utils.kite"
                import * from "imports/string_utils.kite"

                var mySum = add(1, 2)
                var msg = greet("World")
                var greeting = DEFAULT_GREETING
                """);

        // add from named import should work
        assertEquals(3, interpreter.getVar("mySum"));

        // All string_utils symbols should be available via wildcard
        assertEquals("Hello, World", interpreter.getVar("msg"));
        assertEquals("Welcome", interpreter.getVar("greeting"));

        // multiply from math should NOT be available (only add was imported)
        assertFalse(interpreter.hasVar("multiply"));
    }

    @Test
    @DisplayName("should import variable with named import")
    void namedImportVariable() {
        eval("""
                import PI from "imports/math_utils.kite"

                var myPi = PI
                """);

        assertEquals(3.14159, interpreter.getVar("myPi"));
    }

    @Test
    @DisplayName("should import function and use with computed values")
    void namedImportFunctionWithComputedValues() {
        eval("""
                import add, multiply from "imports/math_utils.kite"

                var result = add(multiply(2, 3), multiply(4, 5))
                """);

        // (2 * 3) + (4 * 5) = 6 + 20 = 26
        assertEquals(26, interpreter.getVar("result"));
    }

    // ========== Directory Import Tests ==========

    @Test
    @DisplayName("should import single symbol from directory")
    void directoryImportNamedSymbol() {
        eval("""
                import NatGateway from "providers/networking"

                var natType = NatGateway.resourceType
                var nat = NatGateway.create("my-nat")
                """);

        assertEquals("nat-gateway", interpreter.getVar("natType"));

        // VPC and Subnet symbols should NOT be available (only NatGateway was imported)
        assertFalse(interpreter.hasVar("VPC"), "VPC should NOT be imported");
        assertFalse(interpreter.hasVar("Subnet"), "Subnet should NOT be imported");
    }

    @Test
    @DisplayName("should import multiple symbols from directory")
    void directoryImportMultipleSymbols() {
        eval("""
                import NatGateway, VPC from "providers/networking"

                var natType = NatGateway.resourceType
                var vpcType = VPC.resourceType
                """);

        assertEquals("nat-gateway", interpreter.getVar("natType"));
        assertEquals("vpc", interpreter.getVar("vpcType"));

        // Subnet should NOT be available
        assertFalse(interpreter.hasVar("Subnet"), "Subnet should NOT be imported");
    }

    @Test
    @DisplayName("should import all .kite files from directory with wildcard")
    void directoryImportWildcard() {
        eval("""
                import * from "providers/networking"

                var natType = NatGateway.resourceType
                var vpcType = VPC.resourceType
                var subnetType = Subnet.resourceType
                """);

        assertEquals("nat-gateway", interpreter.getVar("natType"));
        assertEquals("vpc", interpreter.getVar("vpcType"));
        assertEquals("subnet", interpreter.getVar("subnetType"));
    }

    @Test
    @DisplayName("should error when importing non-existent symbol from directory")
    void directoryImportNonExistentSymbol() {
        var exception = assertThrows(RuntimeException.class, () -> eval("""
                import NonExistent from "providers/networking"
                """));

        assertTrue(exception.getMessage().contains("not found") ||
                   exception.getMessage().contains("NonExistent"),
                "Error should mention missing symbol: " + exception.getMessage());
    }

    @Test
    @DisplayName("should error when importing from non-existent directory")
    void directoryImportNonExistentDirectory() {
        var exception = assertThrows(RuntimeException.class, () -> eval("""
                import Something from "non/existent/directory"
                """));

        assertTrue(exception.getMessage().contains("not found") ||
                   exception.getMessage().contains("directory"),
                "Error should mention missing directory: " + exception.getMessage());
    }

    @Test
    @DisplayName("should use functions from directory imports")
    void directoryImportUseFunctions() {
        eval("""
                import VPC from "providers/networking"

                var myVpc = VPC.create("main-vpc", "10.0.0.0/16")
                """);

        var vpc = interpreter.getVar("myVpc");
        assertNotNull(vpc);
        assertInstanceOf(java.util.Map.class, vpc);
        @SuppressWarnings("unchecked")
        var vpcMap = (java.util.Map<String, Object>) vpc;
        assertEquals("vpc", vpcMap.get("resourceType"));
        assertEquals("main-vpc", vpcMap.get("name"));
    }

    @Test
    @DisplayName("should only expose named symbol, not internal file symbols")
    void directoryImportOnlyExposesNamedSymbol() {
        eval("""
                import VPC from "providers/networking"

                var vpcType = VPC.resourceType
                """);

        // VPC object should be available
        assertTrue(interpreter.hasVar("VPC"), "VPC should be imported");
        assertEquals("vpc", interpreter.getVar("vpcType"));

        // Internal symbols from VPC.kite file should NOT be exposed at top level
        // createVPC function is internal to VPC.kite and should not be available
        assertFalse(interpreter.hasVar("createVPC"), "createVPC should NOT be at top level");
        assertFalse(interpreter.hasVar("defaultCidr"), "defaultCidr should NOT be at top level");
    }

    // ========== Directory Import Tests - Schemas, Components, Resources ==========

    @Test
    @DisplayName("should import schema from directory")
    void directoryImportSchema() {
        eval("""
                import Instance from "providers/compute"

                resource Instance myServer {
                    name = "web-server"
                    instanceType = "t2.large"
                }
                """);

        // Instance schema should be available
        assertTrue(interpreter.hasVar("Instance"), "Instance schema should be imported");

        // Check that the resource was created
        assertTrue(interpreter.hasVar("myServer"), "Resource myServer should be created");
    }

    @Test
    @DisplayName("should import schema with wildcard from directory")
    void directoryImportSchemaWildcard() {
        eval("""
                import * from "providers/compute"

                resource Instance myInstance {
                    name = "test-instance"
                }

                resource Database myDb {
                    name = "app-db"
                    engine = "mysql"
                }
                """);

        // Both schemas should be available
        assertTrue(interpreter.hasVar("Instance"), "Instance schema should be imported");
        assertTrue(interpreter.hasVar("Database"), "Database schema should be imported");
        assertTrue(interpreter.hasVar("myInstance"), "Resource myInstance should be created");
        assertTrue(interpreter.hasVar("myDb"), "Resource myDb should be created");
    }

    @Test
    @DisplayName("should import multiple schemas and variables from directory with wildcard")
    void directoryImportMultipleTypesWildcard() {
        eval("""
                import * from "providers/compute"

                resource Instance server {
                    name = "app-server"
                }

                resource Database myDb {
                    name = "app-db"
                }

                var dbDefaults = DatabaseDefaults
                """);

        // All schemas should be available
        assertTrue(interpreter.hasVar("Instance"), "Instance schema should be imported");
        assertTrue(interpreter.hasVar("Database"), "Database schema should be imported");
        assertTrue(interpreter.hasVar("DatabaseDefaults"), "DatabaseDefaults variable should be imported");

        // Resources should be created
        assertTrue(interpreter.hasVar("server"), "Resource server should be created");
        assertTrue(interpreter.hasVar("myDb"), "Resource myDb should be created");

        // Variable from imported module should be accessible
        var dbDefaults = interpreter.getVar("dbDefaults");
        assertNotNull(dbDefaults);
        assertInstanceOf(java.util.Map.class, dbDefaults);
    }

    @Test
    @DisplayName("should import named schema and use helper function from directory")
    void directoryImportSchemaWithHelperFunction() {
        eval("""
                import Instance, createInstanceConfig from "providers/compute"

                var config = createInstanceConfig("my-server", "t3.xlarge")
                """);

        // Both schema and helper function should be available
        assertTrue(interpreter.hasVar("Instance"), "Instance schema should be imported");
        assertTrue(interpreter.hasVar("createInstanceConfig"), "createInstanceConfig function should be imported");

        var config = interpreter.getVar("config");
        assertNotNull(config);
        assertInstanceOf(java.util.Map.class, config);

        @SuppressWarnings("unchecked")
        var configMap = (java.util.Map<String, Object>) config;
        assertEquals("my-server", configMap.get("name"));
        assertEquals("t3.xlarge", configMap.get("instanceType"));
    }

    // ========== Directory Import Tests - Creating Resources with Imported Schemas ==========

    @Test
    @DisplayName("should import schema from directory and create resource")
    void directoryImportSchemaAndCreateResource() {
        eval("""
                import ServerConfig from "providers/compute"

                resource ServerConfig webServer {
                    name = "web-server"
                    size = "large"
                }

                resource ServerConfig apiServer {
                    name = "api-server"
                }
                """);

        // Schema should be imported
        assertTrue(interpreter.hasVar("ServerConfig"), "ServerConfig schema should be imported");

        // Resources should be created
        assertTrue(interpreter.hasVar("webServer"), "webServer should be created");
        assertTrue(interpreter.hasVar("apiServer"), "apiServer should be created");
    }

    @Test
    @DisplayName("should import schema and variable from directory")
    void directoryImportSchemaAndVariable() {
        eval("""
                import ServerConfig, serverCount from "providers/compute"

                resource ServerConfig myServer {
                    name = "my-server"
                }

                var count = serverCount
                """);

        // Schema and variable should be imported
        assertTrue(interpreter.hasVar("ServerConfig"), "ServerConfig schema should be imported");
        assertTrue(interpreter.hasVar("serverCount"), "serverCount should be imported");
        assertTrue(interpreter.hasVar("myServer"), "myServer should be created");

        assertEquals(2, interpreter.getVar("count"));
    }
}
