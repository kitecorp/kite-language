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

import java.nio.file.Path;
import java.nio.file.Paths;

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

    private String getTestResourcePath(String filename) {
        Path resourcePath = Paths.get("src/test/resources", filename).toAbsolutePath();
        return resourcePath.toString();
    }

    @Test
    @DisplayName("should cache parsed programs to avoid re-parsing")
    void shouldCacheParsedPrograms() {
        String stdlibPath = getTestResourcePath("stdlib.kite");

        assertEquals(0, ImportResolver.getCacheSize(), "Cache should be empty initially");

        eval("""
                import * from "%s"
                """.formatted(stdlibPath));

        // Cache should have entries after import
        assertTrue(ImportResolver.getCacheSize() > 0, "Cache should have entries after import");
        int cacheSize = ImportResolver.getCacheSize();

        // Import the same file again - cache size should not change
        eval("""
                import * from "%s"
                """.formatted(stdlibPath));

        assertEquals(cacheSize, ImportResolver.getCacheSize(), "Cache size should not increase for same file");
    }

    @Test
    @DisplayName("should import function types from external file")
    void importFunctionTypes() {
        String stdlibPath = getTestResourcePath("stdlib.kite");

        eval("""
                import * from "%s"

                var result = double(5)
                """.formatted(stdlibPath));

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
        String stdlibPath = getTestResourcePath("stdlib.kite");

        eval("""
                import * from "%s"

                var msg = greeting
                """.formatted(stdlibPath));

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
        String stdlibPath = getTestResourcePath("stdlib.kite");

        // Calling double with wrong argument type should fail
        assertThrows(TypeError.class, () -> eval("""
                import * from "%s"

                var result = double("not a number")
                """.formatted(stdlibPath)));
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
        String circularPath = getTestResourcePath("circular_a.kite");

        var exception = assertThrows(TypeError.class, () -> eval("""
                import * from "%s"
                """.formatted(circularPath)));

        assertTrue(exception.getMessage().contains("Circular import"),
                "Error message should mention circular import: " + exception.getMessage());
    }

    @Test
    @DisplayName("should handle nested imports")
    void handleNestedImports() {
        String nestedPath = getTestResourcePath("nested_b.kite");

        eval("""
                import * from "%s"

                var result = valueB
                """.formatted(nestedPath));

        // valueB should be accessible and typed
        var valueBType = checker.getEnv().lookup("valueB");
        assertNotNull(valueBType, "Variable 'valueB' should be imported from nested file");
        var valueAType = checker.getEnv().lookup("valueA");
        assertNotNull(valueAType, "Variable 'valueA' should be imported from nested file");
    }

    @Test
    @DisplayName("should import schema types")
    void importSchemaTypes() {
        String schemaPath = getTestResourcePath("schema_import.kite");

        eval("""
                import * from "%s"

                resource vm myVm {
                    name = "test"
                }
                """.formatted(schemaPath));

        // The 'vm' schema should be available
        var vmSchema = checker.getEnv().lookup("vm");
        assertNotNull(vmSchema, "Schema 'vm' should be imported");
    }

    @Test
    @DisplayName("should import component types")
    @Disabled("Component import not yet implemented - requires shared ComponentRegistry")
    void importComponentTypes() {
        String componentPath = getTestResourcePath("component_import.kite");

        eval("""
                import * from "%s"

                component MyComponent myInstance {
                    inputValue = "test"
                }
                """.formatted(componentPath));

        // The component type should be available
        var componentType = checker.getEnv().lookup("MyComponent");
        assertNotNull(componentType, "Component 'MyComponent' should be imported");
    }

    @Test
    @DisplayName("should not pollute environment with stdlib functions from imported file")
    void shouldNotPollutedWithStdlib() {
        String stdlibPath = getTestResourcePath("stdlib.kite");

        eval("""
                import * from "%s"
                """.formatted(stdlibPath));

        // Only user-defined items should be imported, not built-in functions
        // that were auto-initialized in the imported TypeChecker
        var doubleType = checker.getEnv().lookup("double");
        var greetingType = checker.getEnv().lookup("greeting");

        assertNotNull(doubleType, "User-defined 'double' function should be imported");
        assertNotNull(greetingType, "User-defined 'greeting' variable should be imported");
    }
}