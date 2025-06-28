package io.zmeu.Base;

import io.zmeu.Frontend.Parse.ParserTest;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Runtime.Environment.Environment;
import io.zmeu.TypeChecker.TypeChecker;
import org.junit.jupiter.api.BeforeEach;

public class CheckerTest extends ParserTest {
    protected TypeChecker checker;
    protected Program program;

    @BeforeEach
    void setUp() {
        checker = new TypeChecker();
    }

    @Override
    protected Object eval(String source) {
        program = super.src(source);
        return checker.visit(program);
    }

    protected Environment getEnvironment() {
        return checker.getEnv();
    }
}
