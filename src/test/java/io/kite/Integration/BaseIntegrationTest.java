package io.kite.Integration;

import io.kite.Base.RuntimeTest;
import io.kite.Runtime.Inputs.InputChainResolver;
import io.kite.TypeChecker.TypeChecker;
import org.junit.jupiter.api.AfterEach;

public class BaseIntegrationTest extends RuntimeTest {
    protected TypeChecker typeChecker;
    protected InputChainResolver inputChainResolver;

    @Override
    protected void init() {
        super.init();
        typeChecker = new TypeChecker();
        inputChainResolver = new InputChainResolver();
    }

    @AfterEach
    void cleanupChecker() {
        typeChecker = null;
    }

    protected Object eval(String source) {
        program = parse(source);
        scopeResolver.resolve(program);
        inputChainResolver.visit(program);
        typeChecker.visit(program);
        return interpreter.visit(program);
    }
}
