package io.zmeu.TypeChecker;

import io.zmeu.Frontend.Parse.BaseTest;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Runtime.Environment.Environment;
import org.junit.jupiter.api.BeforeEach;

public class BaseChecker extends BaseTest {
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
