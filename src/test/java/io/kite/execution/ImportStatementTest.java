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
        assertEquals(10, interpreter.getEnv().lookup("result"));
    }

    @Test
    void importStatementAccessVariable() {
        String stdlibPath = getTestResourcePath("stdlib.kite");
        var code = """
                import * from "%s"
                
                var msg = greeting
                """.formatted(stdlibPath);
        var result = eval(code);
        assertEquals("Hello from stdlib!", interpreter.getEnv().lookup("msg"));
    }

    @Test
    void importStatementCallFunction() {
        String stdlibPath = getTestResourcePath("stdlib.kite");
        var code = """
                import * from "%s"
                
                var doubled = double(10)
                var tripled = triple(5)
                """.formatted(stdlibPath);
        eval(code);
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
}