package io.kite.Base;

import io.kite.TypeChecker.TypeChecker;
import org.junit.jupiter.api.BeforeEach;

public class SrcTest extends RuntimeTest {
    protected TypeChecker checker;

    @BeforeEach
    void setUp() {
        checker = new TypeChecker();
    }

    protected Object eval(String source) {
        program = super.src(source);        // parse
        resolver.resolve(program);          // resolve block scopes inconsistencies
        checker.visit(program);             // check types
        return interpreter.visit(program);  // interpret
    }
}
