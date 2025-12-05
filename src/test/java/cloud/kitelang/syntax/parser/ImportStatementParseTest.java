package cloud.kitelang.syntax.parser;

import cloud.kitelang.syntax.ast.KiteCompiler;
import cloud.kitelang.syntax.ast.statements.ImportStatement;
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

    // ========== Named Import Tests ==========

    @Test
    void parseNamedImportSingleSymbol() {
        var code = """
                import myFunc from "utils.kite"
                """;
        var program = compiler.parse(code);

        assertEquals(1, program.getBody().size());
        assertTrue(program.getBody().get(0) instanceof ImportStatement);

        ImportStatement importStmt = (ImportStatement) program.getBody().get(0);
        assertEquals("utils.kite", importStmt.getFilePath());
        assertEquals(1, importStmt.getSymbols().size());
        assertEquals("myFunc", importStmt.getSymbols().get(0));
        assertTrue(!importStmt.isImportAll(), "Named import should not be import all");
    }

    @Test
    void parseNamedImportMultipleSymbols() {
        var code = """
                import add, multiply, PI from "math.kite"
                """;
        var program = compiler.parse(code);

        assertEquals(1, program.getBody().size());
        ImportStatement importStmt = (ImportStatement) program.getBody().get(0);

        assertEquals("math.kite", importStmt.getFilePath());
        assertEquals(3, importStmt.getSymbols().size());
        assertTrue(importStmt.getSymbols().contains("add"));
        assertTrue(importStmt.getSymbols().contains("multiply"));
        assertTrue(importStmt.getSymbols().contains("PI"));
        assertTrue(!importStmt.isImportAll());
    }

    @Test
    void parseImportAllIsImportAll() {
        var code = """
                import * from "stdlib.kite"
                """;
        var program = compiler.parse(code);

        ImportStatement importStmt = (ImportStatement) program.getBody().get(0);
        assertTrue(importStmt.isImportAll(), "Import * should be import all");
        assertTrue(importStmt.getSymbols().isEmpty(), "Import * should have empty symbols list");
    }

    @Test
    void parseMixedImports() {
        var code = """
                import * from "stdlib.kite"
                import add, multiply from "math.kite"
                import greet from "strings.kite"
                """;
        var program = compiler.parse(code);

        assertEquals(3, program.getBody().size());

        ImportStatement import1 = (ImportStatement) program.getBody().get(0);
        assertTrue(import1.isImportAll());
        assertEquals("stdlib.kite", import1.getFilePath());

        ImportStatement import2 = (ImportStatement) program.getBody().get(1);
        assertTrue(!import2.isImportAll());
        assertEquals(2, import2.getSymbols().size());
        assertEquals("math.kite", import2.getFilePath());

        ImportStatement import3 = (ImportStatement) program.getBody().get(2);
        assertTrue(!import3.isImportAll());
        assertEquals(1, import3.getSymbols().size());
        assertEquals("greet", import3.getSymbols().get(0));
    }
}