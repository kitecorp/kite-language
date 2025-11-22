package io.kite.syntax.parser;

import io.kite.syntax.ast.KiteCompiler;
import io.kite.syntax.ast.statements.ImportStatement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImportStatementParseTest {

    private final KiteCompiler compiler = new KiteCompiler();

    @Test
    void parseImportStatement() {
        var code = """
                import * from "stdlib.kite"
                """;
        var program = compiler.parse(code);

        System.out.println("Program body size: " + program.getBody().size());
        for (int i = 0; i < program.getBody().size(); i++) {
            System.out.println("Statement " + i + ": " + program.getBody().get(i).getClass().getName());
        }

        assertEquals(1, program.getBody().size(), "Expected 1 statement in program body");
        assertTrue(program.getBody().get(0) instanceof ImportStatement,
                "Expected ImportStatement but got: " + program.getBody().get(0).getClass().getName());

        ImportStatement importStmt = (ImportStatement) program.getBody().get(0);
        assertEquals("stdlib.kite", importStmt.getFilePath());
    }

    @Test
    void parseImportStatementWithAbsolutePath() {
        var code = """
                import * from "/absolute/path/to/file.kite"
                """;
        var program = compiler.parse(code);

        assertEquals(1, program.getBody().size());
        assertTrue(program.getBody().get(0) instanceof ImportStatement);

        ImportStatement importStmt = (ImportStatement) program.getBody().get(0);
        assertEquals("/absolute/path/to/file.kite", importStmt.getFilePath());
    }

    @Test
    void parseMultipleImports() {
        var program = compiler.parse("""
                import * from "file1.kite"
                import * from "file2.kite"
                """);

        assertEquals(2, program.getBody().size());
        assertTrue(program.getBody().get(0) instanceof ImportStatement);
        assertTrue(program.getBody().get(1) instanceof ImportStatement);

        ImportStatement import1 = (ImportStatement) program.getBody().get(0);
        ImportStatement import2 = (ImportStatement) program.getBody().get(1);

        assertEquals("file1.kite", import1.getFilePath());
        assertEquals("file2.kite", import2.getFilePath());
    }

    @Test
    void parseImportWithCodeAfter() {
        var code = """
                import * from "stdlib.kite"
                
                var x = 10
                """;
        var program = compiler.parse(code);

        assertEquals(2, program.getBody().size());
        assertTrue(program.getBody().get(0) instanceof ImportStatement);
    }
}