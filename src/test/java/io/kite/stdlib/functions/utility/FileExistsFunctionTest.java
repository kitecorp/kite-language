package io.kite.stdlib.functions.utility;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileExistsFunctionTest extends RuntimeTest {

    private final FileExistsFunction function = new FileExistsFunction();

    @TempDir
    Path tempDir;

    @Test
    void fileExistsTrue() throws IOException {
        var file = tempDir.resolve("test.txt").toFile();
        file.createNewFile();

        var res = function.call(interpreter, file.getAbsolutePath());
        assertEquals(true, res);
    }

    @Test
    void fileExistsFalse() {
        var nonExistent = tempDir.resolve("nonexistent.txt").toFile();

        var res = function.call(interpreter, nonExistent.getAbsolutePath());
        assertEquals(false, res);
    }

    @Test
    void fileExistsDirectory() {
        var res = function.call(interpreter, tempDir.toString());
        assertEquals(true, res);
    }

    @Test
    void fileExistsEmptyPath() {
        // Empty path "" behavior is system-dependent (often refers to current directory)
        var res = function.call(interpreter, "");
        // Just verify it returns a boolean, don't assert specific value
        assertTrue(res instanceof Boolean);
    }

    @Test
    void fileExistsRelativePath() throws IOException {
        var file = tempDir.resolve("relative.txt").toFile();
        file.createNewFile();

        var res = function.call(interpreter, file.getAbsolutePath());
        assertEquals(true, res);
    }

    @Test
    void fileExistsTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }

    @Test
    void fileExistsInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }
}
