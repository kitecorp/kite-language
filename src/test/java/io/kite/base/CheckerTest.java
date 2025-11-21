package io.kite.base;

import io.kite.semantics.TypeChecker;
import io.kite.semantics.types.Type;
import io.kite.syntax.ast.Program;
import io.kite.syntax.parser.ParserTest;
import io.kite.tool.theme.PlainTheme;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class CheckerTest extends ParserTest {
    protected TypeChecker checker;
    protected Program program;

    @BeforeEach
    void setUp() {
        checker = new TypeChecker();
        checker.getPrinter().setTheme(new PlainTheme());
    }

    @AfterEach
    void cleanup() {
        program = null;
        checker = null;
    }

    @Override
    protected Type eval(String source) {
        program = super.parse(source);
        return checker.visit(program);
    }

}
