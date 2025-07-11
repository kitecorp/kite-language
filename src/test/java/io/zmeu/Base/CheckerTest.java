package io.zmeu.Base;

import io.zmeu.Frontend.Parse.ParserTest;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.TypeChecker.TypeChecker;
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
    protected Object eval(String source) {
        program = super.src(source);
        return checker.visit(program);
    }

}
