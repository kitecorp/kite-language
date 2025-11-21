package io.kite.base;

import io.kite.frontend.parse.ParserTest;
import io.kite.frontend.parser.Program;
import io.kite.typechecker.TypeChecker;
import io.kite.typechecker.types.Type;
import io.kite.visitors.PlainTheme;
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
