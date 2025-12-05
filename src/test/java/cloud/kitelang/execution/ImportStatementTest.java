package cloud.kitelang.execution;

import cloud.kitelang.analysis.ImportResolver;
import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Interpreter Import Statement")
class ImportStatementTest extends RuntimeTest {

    @BeforeEach
    void clearImportCache() {
        ImportResolver.clearCache();
    }

    private String getTestResourcePath(String filename) {
        // Get the absolute path to the test resources directory
        Path resourcePath = Paths.get("src/test/resources", filename).toAbsolutePath();
        return resourcePath.toString();
    }

    @Test
    void importStatementBasic() {
        String stdlibPath = getTestResourcePath("stdlib.kite");

        // Now try the full eval
        var res = eval("""
                import * from "%s"
                
                var result = double(5)
                """.formatted(stdlibPath));

        assertNotNull(res);
        // Verify the imported function was called successfully (5 * 2 = 10 as integer)
        assertEquals(10, interpreter.getVar("result"));
    }

    @Test
    void importStatementAccessVariable() {
        String stdlibPath = getTestResourcePath("stdlib.kite");
        eval("""
                import * from "%s"
                
                var msg = greeting
                """.formatted(stdlibPath));
        assertEquals("Hello from stdlib!", interpreter.getVar("msg"));
    }

    @Test
    void importStatementCallFunction() {
        String stdlibPath = getTestResourcePath("stdlib.kite");
        eval("""
                import * from "%s"
                
                var doubled = double(10)
                var tripled = triple(5)
                """.formatted(stdlibPath));
        assertEquals(20, interpreter.getEnv().lookup("doubled"));  // 10 * 2 = 20 (integer)
        assertEquals(15, interpreter.getEnv().lookup("tripled"));  // 5 * 3 = 15 (integer)
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
        String nestedPath = getTestResourcePath("nested_b.kite");
        var code = """
                import * from "%s"
                
                var result = valueB
                """.formatted(nestedPath);
        eval(code);

        // valueB should be 10 (from nested_b.kite)
        // which uses valueA (6 from nested_a.kite, which is double(3) from stdlib)
        assertEquals(10, interpreter.getVar("result"));
    }

    @Test
    void importStatementCircular() {
        // Test circular import detection: circular_a imports circular_b, which imports circular_a
        String circularPath = getTestResourcePath("circular_a.kite");
        var code = """
                import * from "%s"
                """.formatted(circularPath);

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
        String stdlibPath = getTestResourcePath("stdlib.kite");

        assertEquals(0, ImportResolver.getCacheSize(), "Cache should be empty initially");

        eval("""
                import * from "%s"
                """.formatted(stdlibPath));

        assertTrue(ImportResolver.getCacheSize() > 0, "Cache should have entries after import");
        int cacheSize = ImportResolver.getCacheSize();

        // Import the same file again - cache size should not change
        eval("""
                import * from "%s"
                """.formatted(stdlibPath));

        assertEquals(cacheSize, ImportResolver.getCacheSize(), "Cache size should not increase for same file");
    }

    @Test
    @DisplayName("should handle multiple imports in same file")
    void multipleImportsInSameFile() {
        String mathPath = getTestResourcePath("imports/math_utils.kite");
        String stringPath = getTestResourcePath("imports/string_utils.kite");

        eval("""
                import * from "%s"
                import * from "%s"

                var mySum = add(1, 2)
                var myProduct = multiply(3, 4)
                var myMessage = greet("World")
                var myPi = PI
                var myGreeting = DEFAULT_GREETING
                """.formatted(mathPath, stringPath));

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
        String topPath = getTestResourcePath("imports/diamond_top.kite");

        eval("""
                import * from "%s"

                var combined = COMBINED
                var result = process(5)
                """.formatted(topPath));

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
        String topPath = getTestResourcePath("imports/diamond_top.kite");

        assertEquals(0, ImportResolver.getCacheSize(), "Cache should be empty initially");

        eval("""
                import * from "%s"
                """.formatted(topPath));

        // Cache should contain: diamond_top, diamond_left, diamond_right, common
        // common.kite is imported by both left and right but should only be parsed once
        int cacheSize = ImportResolver.getCacheSize();
        assertEquals(4, cacheSize, "Cache should have exactly 4 entries (common parsed once)");
    }

    @Test
    @DisplayName("should allow importing same file multiple times explicitly")
    void importSameFileMultipleTimes() {
        String mathPath = getTestResourcePath("imports/math_utils.kite");

        // Importing the same file twice should work (idempotent)
        eval("""
                import * from "%s"
                import * from "%s"

                var result = add(1, multiply(2, 3))
                """.formatted(mathPath, mathPath));

        // 1 + (2 * 3) = 1 + 6 = 7
        assertEquals(7, interpreter.getVar("result"));
    }

    @Test
    @DisplayName("should execute functions from multiple imports together")
    void executeFunctionsFromMultipleImports() {
        String mathPath = getTestResourcePath("imports/math_utils.kite");
        String stringPath = getTestResourcePath("imports/string_utils.kite");

        eval("""
                import * from "%s"
                import * from "%s"

                var myNum = add(10, 20)
                var myMsg = greet("Kite")
                var myCombined = myMsg + " - result"
                """.formatted(mathPath, stringPath));

        assertEquals(30, interpreter.getVar("myNum"));
        assertEquals("Hello, Kite", interpreter.getVar("myMsg"));
        assertEquals("Hello, Kite - result", interpreter.getVar("myCombined"));
    }

    @Test
    @DisplayName("should handle deeply nested function calls across imports")
    void deeplyNestedFunctionCalls() {
        String mathPath = getTestResourcePath("imports/math_utils.kite");

        eval("""
                import * from "%s"

                var result = add(multiply(2, 3), multiply(4, 5))
                """.formatted(mathPath));

        // (2 * 3) + (4 * 5) = 6 + 20 = 26
        assertEquals(26, interpreter.getVar("result"));
    }

    @Test
    @DisplayName("should use imported variables in expressions")
    void useImportedVariablesInExpressions() {
        String mathPath = getTestResourcePath("imports/math_utils.kite");

        eval("""
                import * from "%s"

                var circumference = 2 * PI * 10
                """.formatted(mathPath));

        // 2 * 3.14159 * 10 = 62.8318
        var result = (Number) interpreter.getVar("circumference");
        assertEquals(62.8318, result.doubleValue(), 0.001);
    }

    @Test
    @DisplayName("should handle import chain with value propagation")
    void importChainWithValuePropagation() {
        // nested_b imports nested_a which imports stdlib
        // This tests that values computed in nested imports are correctly propagated
        String nestedPath = getTestResourcePath("nested_b.kite");

        eval("""
                import * from "%s"

                var a = valueA
                var b = valueB
                """.formatted(nestedPath));

        // valueA = double(3) = 6 (from nested_a)
        // valueB = valueA + 4 = 10 (from nested_b)
        assertEquals(6, interpreter.getVar("a"));
        assertEquals(10, interpreter.getVar("b"));
    }
}