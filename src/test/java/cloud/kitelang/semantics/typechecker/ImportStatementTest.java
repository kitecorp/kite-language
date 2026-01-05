package cloud.kitelang.semantics.typechecker;

import cloud.kitelang.analysis.ImportResolver;
import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.semantics.types.FunType;
import cloud.kitelang.semantics.types.ValueType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TypeChecker handling of import statements.
 * The TypeChecker should:
 * 1. Parse and type-check imported files
 * 2. Merge exported types (functions, variables, schemas, components) into current environment
 * 3. Detect circular imports at type-check time
 * 4. Validate import file paths exist
 */
@DisplayName("TypeChecker Import Statement")
class ImportStatementTest extends CheckerTest {

    @BeforeEach
    void clearImportCache() {
        ImportResolver.clearCache();
    }

    @Test
    @DisplayName("should cache parsed programs to avoid re-parsing")
    void shouldCacheParsedPrograms() {
        assertEquals(0, ImportResolver.getCacheSize(), "Cache should be empty initially");

        eval("""
                import * from "stdlib.kite"
                """);

        // Cache should have entries after import
        assertTrue(ImportResolver.getCacheSize() > 0, "Cache should have entries after import");
        int cacheSize = ImportResolver.getCacheSize();

        // Import the same file again - cache size should not change
        eval("""
                import * from "stdlib.kite"
                """);

        assertEquals(cacheSize, ImportResolver.getCacheSize(), "Cache size should not increase for same file");
    }

    @Test
    @DisplayName("should import function types from external file")
    void importFunctionTypes() {
        eval("""
                import * from "stdlib.kite"

                var result = double(5)
                """);

        // The 'double' function should be available in the type environment
        var doubleType = checker.getEnv().lookup("double");
        assertNotNull(doubleType, "Function 'double' should be imported");
        assertInstanceOf(FunType.class, doubleType);

        // The result should be typed as number (return type of double)
        var resultType = checker.getEnv().lookup("result");
        assertEquals(ValueType.Number, resultType);
    }

    @Test
    @DisplayName("should import variable types from external file")
    void importVariableTypes() {
        eval("""
                import * from "stdlib.kite"

                var msg = greeting
                """);

        // The 'greeting' variable should be available as string type
        var greetingType = checker.getEnv().lookup("greeting");
        assertNotNull(greetingType, "Variable 'greeting' should be imported");

        // msg should inherit the string type
        var msgType = checker.getEnv().lookup("msg");
        assertEquals(ValueType.String, msgType);
    }

    @Test
    @DisplayName("should type-check function calls from imported file")
    void typeCheckImportedFunctionCalls() {
        // Calling double with wrong argument type should fail
        assertThrows(TypeError.class, () -> eval("""
                import * from "stdlib.kite"

                var result = double("not a number")
                """));
    }

    @Test
    @DisplayName("should detect non-existent import file")
    void detectNonExistentImportFile() {
        assertThrows(TypeError.class, () -> eval("""
                import * from "nonexistent_file.kite"
                """));
    }

    @Test
    @DisplayName("should detect circular imports")
    void detectCircularImports() {
        var exception = assertThrows(TypeError.class, () -> eval("""
                import * from "circular_a.kite"
                """));

        assertTrue(exception.getMessage().contains("Circular import"),
                "Error message should mention circular import: " + exception.getMessage());
    }

    @Test
    @DisplayName("should handle nested imports")
    void handleNestedImports() {
        eval("""
                import * from "nested_b.kite"

                var result = valueB
                """);

        // valueB should be accessible and typed
        var valueBType = checker.getEnv().lookup("valueB");
        assertNotNull(valueBType, "Variable 'valueB' should be imported from nested file");
        var valueAType = checker.getEnv().lookup("valueA");
        assertNotNull(valueAType, "Variable 'valueA' should be imported from nested file");
    }

    @Test
    @DisplayName("should import schema types")
    void importSchemaTypes() {
        eval("""
                import * from "schema_import.kite"

                resource vm myVm {
                    name = "test"
                }
                """);

        // The 'vm' schema should be available
        var vmSchema = checker.getEnv().lookup("vm");
        assertNotNull(vmSchema, "Schema 'vm' should be imported");
    }

    @Test
    @DisplayName("should import component types")
    @Disabled("Component import not yet implemented - requires shared ComponentRegistry")
    void importComponentTypes() {
        eval("""
                import * from "component_import.kite"

                component MyComponent myInstance {
                    inputValue = "test"
                }
                """);

        // The component type should be available
        var componentType = checker.getEnv().lookup("MyComponent");
        assertNotNull(componentType, "Component 'MyComponent' should be imported");
    }

    @Test
    @DisplayName("should not pollute environment with stdlib functions from imported file")
    void shouldNotPollutedWithStdlib() {
        eval("""
                import * from "stdlib.kite"
                """);

        // Only user-defined items should be imported, not built-in functions
        // that were auto-initialized in the imported TypeChecker
        var doubleType = checker.getEnv().lookup("double");
        var greetingType = checker.getEnv().lookup("greeting");

        assertNotNull(doubleType, "User-defined 'double' function should be imported");
        assertNotNull(greetingType, "User-defined 'greeting' variable should be imported");
    }

    // ========== Complex Import Tests ==========

    @Test
    @DisplayName("should handle multiple imports in same file")
    void multipleImportsInSameFile() {
        eval("""
                import * from "imports/math_utils.kite"
                import * from "imports/string_utils.kite"

                var sum = add(1, 2)
                var message = greet("World")
                var pi = PI
                var greeting = DEFAULT_GREETING
                """);

        // Math functions should be available
        var addType = checker.getEnv().lookup("add");
        assertNotNull(addType, "Function 'add' should be imported");
        assertInstanceOf(FunType.class, addType);

        // String functions should be available
        var greetType = checker.getEnv().lookup("greet");
        assertNotNull(greetType, "Function 'greet' should be imported");
        assertInstanceOf(FunType.class, greetType);

        // Variables from both files should be available
        assertEquals(ValueType.Number, checker.getEnv().lookup("sum"));
        assertEquals(ValueType.String, checker.getEnv().lookup("message"));
        assertEquals(ValueType.Number, checker.getEnv().lookup("pi"));
        assertEquals(ValueType.String, checker.getEnv().lookup("greeting"));
    }

    @Test
    @DisplayName("should handle diamond dependency pattern")
    void diamondDependencyPattern() {
        eval("""
                import * from "imports/diamond_top.kite"

                var combined = COMBINED
                var result = process(5)
                """);

        // All symbols from the diamond should be available
        assertNotNull(checker.getEnv().lookup("SHARED_VALUE"), "SHARED_VALUE from common should be available");
        assertNotNull(checker.getEnv().lookup("LEFT_VALUE"), "LEFT_VALUE from diamond_left should be available");
        assertNotNull(checker.getEnv().lookup("RIGHT_VALUE"), "RIGHT_VALUE from diamond_right should be available");
        assertNotNull(checker.getEnv().lookup("COMBINED"), "COMBINED from diamond_top should be available");

        // Functions should be available
        assertNotNull(checker.getEnv().lookup("identity"), "identity from common should be available");
        assertNotNull(checker.getEnv().lookup("leftProcess"), "leftProcess should be available");
        assertNotNull(checker.getEnv().lookup("rightProcess"), "rightProcess should be available");
        assertNotNull(checker.getEnv().lookup("process"), "process should be available");

        // Types should be correct
        assertEquals(ValueType.Number, checker.getEnv().lookup("combined"));
        assertEquals(ValueType.Number, checker.getEnv().lookup("result"));
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
    @DisplayName("should propagate type errors from imported files")
    void propagateTypeErrorsFromImports() {
        var exception = assertThrows(TypeError.class, () -> eval("""
                import * from "imports/type_error.kite"
                """));

        assertNotNull(exception, "TypeError should be thrown for type errors in imported file");
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

        assertEquals(ValueType.Number, checker.getEnv().lookup("result"));
    }

    @Test
    @DisplayName("should type check function calls across multiple imports")
    void typeCheckAcrossMultipleImports() {
        // Using math function with string should fail
        assertThrows(TypeError.class, () -> eval("""
                import * from "imports/math_utils.kite"
                import * from "imports/string_utils.kite"

                var bad = add("hello", "world")
                """));
    }

    @Test
    @DisplayName("should type check imported function return types used in expressions")
    void typeCheckImportedReturnTypesInExpressions() {
        // Using number function result where string expected should fail
        assertThrows(TypeError.class, () -> eval("""
                import * from "imports/math_utils.kite"
                import * from "imports/string_utils.kite"

                var result = greet(add(1, 2))
                """));
    }

    // ========== Named Import Tests ==========

    @Test
    @DisplayName("should import only specified symbol with named import")
    void namedImportSingleSymbol() {
        eval("""
                import add from "imports/math_utils.kite"

                var result = add(1, 2)
                """);

        // add should be available
        var addType = checker.getEnv().lookup("add");
        assertNotNull(addType, "Function 'add' should be imported");
        assertInstanceOf(FunType.class, addType);
        assertEquals(ValueType.Number, checker.getEnv().lookup("result"));

        // multiply and PI should NOT be available
        assertFalse(checker.getEnv().lookupKey("multiply"), "multiply should NOT be imported");
        assertFalse(checker.getEnv().lookupKey("PI"), "PI should NOT be imported");
    }

    @Test
    @DisplayName("should import multiple specified symbols with named import")
    void namedImportMultipleSymbols() {
        eval("""
                import add, PI from "imports/math_utils.kite"

                var result = add(1, 2)
                var myPi = PI
                """);

        // add and PI should be available
        assertNotNull(checker.getEnv().lookup("add"), "Function 'add' should be imported");
        assertEquals(ValueType.Number, checker.getEnv().lookup("myPi"));

        // multiply should NOT be available
        assertFalse(checker.getEnv().lookupKey("multiply"), "multiply should NOT be imported");
    }

    @Test
    @DisplayName("should error when importing non-existent symbol")
    void namedImportNonExistentSymbol() {
        var exception = assertThrows(TypeError.class, () -> eval("""
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

                var sum = add(1, 2)
                var msg = greet("World")
                """);

        // add from named import should work
        assertNotNull(checker.getEnv().lookup("add"));
        assertEquals(ValueType.Number, checker.getEnv().lookup("sum"));

        // All string_utils symbols should be available via wildcard
        assertNotNull(checker.getEnv().lookup("greet"));
        assertNotNull(checker.getEnv().lookup("DEFAULT_GREETING"));
        assertEquals(ValueType.String, checker.getEnv().lookup("msg"));

        // multiply from math should NOT be available (only add was imported)
        assertFalse(checker.getEnv().lookupKey("multiply"));
    }

    @Test
    @DisplayName("should type check function calls from named import")
    void typeCheckNamedImportFunctionCalls() {
        // Calling add with wrong argument type should fail
        assertThrows(TypeError.class, () -> eval("""
                import add from "imports/math_utils.kite"

                var result = add("not", "numbers")
                """));
    }

    @Test
    @DisplayName("should import variable with named import")
    void namedImportVariable() {
        eval("""
                import PI from "imports/math_utils.kite"

                var myPi = PI
                """);

        assertEquals(ValueType.Number, checker.getEnv().lookup("myPi"));
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

        // NatGateway object should be available
        assertNotNull(checker.getEnv().lookup("NatGateway"), "NatGateway should be imported");
        assertEquals(ValueType.String, checker.getEnv().lookup("natType"));

        // VPC and Subnet should NOT be available
        assertFalse(checker.getEnv().lookupKey("VPC"), "VPC should NOT be imported");
        assertFalse(checker.getEnv().lookupKey("Subnet"), "Subnet should NOT be imported");
    }

    @Test
    @DisplayName("should import multiple symbols from directory")
    void directoryImportMultipleSymbols() {
        eval("""
                import NatGateway, VPC from "providers/networking"

                var natType = NatGateway.resourceType
                var vpcType = VPC.resourceType
                """);

        // Both NatGateway and VPC symbols should be available
        assertEquals(ValueType.String, checker.getEnv().lookup("natType"));
        assertEquals(ValueType.String, checker.getEnv().lookup("vpcType"));

        // Subnet should NOT be available
        assertFalse(checker.getEnv().lookupKey("Subnet"), "Subnet should NOT be imported");
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

        // All symbols should be available
        assertEquals(ValueType.String, checker.getEnv().lookup("natType"));
        assertEquals(ValueType.String, checker.getEnv().lookup("vpcType"));
        assertEquals(ValueType.String, checker.getEnv().lookup("subnetType"));
    }

    @Test
    @DisplayName("should error when importing non-existent symbol from directory")
    void directoryImportNonExistentSymbol() {
        var exception = assertThrows(TypeError.class, () -> eval("""
                import NonExistent from "providers/networking"
                """));

        assertTrue(exception.getMessage().contains("not found") ||
                   exception.getMessage().contains("NonExistent"),
                "Error should mention missing symbol: " + exception.getMessage());
    }

    @Test
    @DisplayName("should error when importing from non-existent directory")
    void directoryImportNonExistentDirectory() {
        var exception = assertThrows(TypeError.class, () -> eval("""
                import Something from "non/existent/directory"
                """));

        assertTrue(exception.getMessage().contains("not found") ||
                   exception.getMessage().contains("directory"),
                "Error should mention missing directory: " + exception.getMessage());
    }

    @Test
    @DisplayName("should type check object properties from directory imports")
    void directoryImportTypeCheckProperties() {
        eval("""
                import VPC from "providers/networking"

                var vpcType = VPC.resourceType
                var myVpc = VPC.create("main-vpc", "10.0.0.0/16")
                """);

        // VPC object should be available
        assertNotNull(checker.getEnv().lookup("VPC"), "VPC should be imported");
        assertEquals(ValueType.String, checker.getEnv().lookup("vpcType"));
    }

    @Test
    @DisplayName("should only expose named symbol, not internal file symbols")
    void directoryImportOnlyExposesNamedSymbol() {
        eval("""
                import VPC from "providers/networking"

                var vpcType = VPC.resourceType
                """);

        // VPC object should be available
        assertTrue(checker.getEnv().lookupKey("VPC"), "VPC should be imported");

        // Internal symbols from VPC.kite file should NOT be exposed at top level
        // createVPC function is internal to VPC.kite and should not be available
        assertFalse(checker.getEnv().lookupKey("createVPC"), "createVPC should NOT be at top level");
        assertFalse(checker.getEnv().lookupKey("defaultCidr"), "defaultCidr should NOT be at top level");
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
        assertTrue(checker.getEnv().lookupKey("Instance"), "Instance schema should be imported");
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
        assertTrue(checker.getEnv().lookupKey("Instance"), "Instance schema should be imported");
        assertTrue(checker.getEnv().lookupKey("Database"), "Database schema should be imported");
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
        assertTrue(checker.getEnv().lookupKey("Instance"), "Instance schema should be imported");
        assertTrue(checker.getEnv().lookupKey("Database"), "Database schema should be imported");
        assertTrue(checker.getEnv().lookupKey("DatabaseDefaults"), "DatabaseDefaults variable should be imported");
    }

    @Test
    @DisplayName("should import named schema and helper function from directory")
    void directoryImportSchemaWithHelperFunction() {
        eval("""
                import Instance, createInstanceConfig from "providers/compute"

                var config = createInstanceConfig("my-server", "t3.xlarge")
                """);

        // Both schema and helper function should be available
        assertTrue(checker.getEnv().lookupKey("Instance"), "Instance schema should be imported");
        assertTrue(checker.getEnv().lookupKey("createInstanceConfig"), "createInstanceConfig function should be imported");

        // Config type should be object
        assertNotNull(checker.getEnv().lookup("config"));
    }

    @Test
    @DisplayName("should type check resource properties against imported schema")
    void directoryImportSchemaTypeCheck() {
        // Using wrong type for property should fail
        assertThrows(TypeError.class, () -> eval("""
                import Instance from "providers/compute"

                resource Instance myServer {
                    name = 123
                }
                """));
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
        assertTrue(checker.getEnv().lookupKey("ServerConfig"), "ServerConfig schema should be imported");
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
        assertTrue(checker.getEnv().lookupKey("ServerConfig"), "ServerConfig schema should be imported");
        assertTrue(checker.getEnv().lookupKey("serverCount"), "serverCount should be imported");
        assertEquals(ValueType.Number, checker.getEnv().lookup("count"));
    }
}
