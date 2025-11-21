package io.kite.stdlib.functions.utility;

import io.kite.base.RuntimeTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReadFileFunctionTest extends RuntimeTest {

    private final ReadFileFunction function = new ReadFileFunction();

    @TempDir
    Path tempDir;

    @Test
    void readFileBasic() throws IOException {
        var file = tempDir.resolve("test.txt");
        Files.writeString(file, "Hello, World!");

        var res = function.call(interpreter, file.toString());
        assertEquals("Hello, World!", res);
    }

    @Test
    void readFileEmpty() throws IOException {
        var file = tempDir.resolve("empty.txt");
        Files.writeString(file, "");

        var res = function.call(interpreter, file.toString());
        assertEquals("", res);
    }

    @Test
    void readFileMultiline() throws IOException {
        var file = tempDir.resolve("multiline.txt");
        Files.writeString(file, "Line 1\nLine 2\nLine 3");

        var res = function.call(interpreter, file.toString());
        assertEquals("Line 1\nLine 2\nLine 3", res);
    }

    @Test
    void readFileUnicode() throws IOException {
        var file = tempDir.resolve("unicode.txt");
        Files.writeString(file, "Hello 世界 🌍");

        var res = function.call(interpreter, file.toString());
        assertEquals("Hello 世界 🌍", res);
    }

    @Test
    void readFileNonExistent() {
        var nonExistent = tempDir.resolve("nonexistent.txt").toString();
        assertThrows(RuntimeException.class, () -> function.call(interpreter, nonExistent));
    }

    @Test
    void readFileTooFewArgs() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter));
    }

    @Test
    void readFileInvalidType() {
        assertThrows(RuntimeException.class, () -> function.call(interpreter, 123));
    }
}
