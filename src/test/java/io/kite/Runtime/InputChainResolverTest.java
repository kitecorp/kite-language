package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Runtime.Inputs.ChainResolver;
import io.kite.Runtime.exceptions.MissingInputException;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Log4j2
public class InputChainResolverTest extends RuntimeTest {
    private ChainResolver chainResolver;

    @BeforeEach
    void initChain() {
        chainResolver = new ChainResolver();
    }

    @Test
    @DisplayName("Env+File skipped throws on last step")
    void testThrowsOnLastStepCLI() {
        Assertions.assertThrows(MissingInputException.class, () -> chainResolver.visit(InputDeclaration.input("region", TypeIdentifier.type("string"))));
    }

    @Test
    @DisplayName("Env is takes precedence")
    void test() {

        chainResolver.visit(InputDeclaration.input("region", TypeIdentifier.type("string")));
    }

}
