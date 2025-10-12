package io.kite.Integration;

import io.kite.Base.RuntimeTest;
import io.kite.Runtime.Inputs.ChainResolver;
import io.kite.TypeChecker.TypeChecker;
import org.junit.jupiter.api.AfterEach;

public class BaseIntegrationTest extends RuntimeTest {
    protected TypeChecker typeChecker;
    protected ChainResolver chainResolver;

    @Override
    protected void init() {
        super.init();
        typeChecker = new TypeChecker();
        chainResolver = new ChainResolver();
    }

    @AfterEach
    void cleanupChecker() {
        typeChecker = null;
    }

    protected Object eval(String source) {
        program = parse(source);
        scopeResolver.resolve(program);
        chainResolver.visit(program);
        typeChecker.visit(program);
        return interpreter.visit(program);
    }
}
