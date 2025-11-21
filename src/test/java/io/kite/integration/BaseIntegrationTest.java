package io.kite.integration;

import io.kite.base.RuntimeTest;
import io.kite.runtime.inputs.InputChainResolver;
import io.kite.typechecker.TypeChecker;
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
