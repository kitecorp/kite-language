package io.kite.Base;

import io.kite.Frontend.Parse.ParserTest;
import io.kite.Frontend.Parser.Program;
import io.kite.TypeChecker.TypeChecker;
import io.kite.TypeChecker.Types.Type;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class CheckerTest extends ParserTest {
    protected TypeChecker checker;
    protected Program program;

    @BeforeEach
    void setUp() {
        checker = new TypeChecker();
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
