package io.kite.Integration;

import io.kite.Base.RuntimeTest;
import io.kite.TypeChecker.TypeChecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class BaseIntegrationTest extends RuntimeTest {
    protected TypeChecker checker;

    @BeforeEach
    void setUpChecker() {
        checker = new TypeChecker();
    }

    @AfterEach
    void cleanupChecker() {
        checker = null;
    }

    @Override
    protected Object eval(String source) {
        program = super.parse(source);
        return checker.visit(program);
    }

}
