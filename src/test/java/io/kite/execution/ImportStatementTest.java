package io.kite.execution;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ImportStatementTest extends RuntimeTest {

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
        assertEquals(10, interpreter.getEnv().lookup("result"));
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
                "Expected circular import error but got: " + exception.getMessage());
    }
}