package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Runtime.Inputs.ChainResolver;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Log4j2
public class InputChainResolverTest extends RuntimeTest {
    private ChainResolver chainResolver;

    @BeforeEach
    void initChain() {
        chainResolver = new ChainResolver();
    }

    @Test
    void testOrder() {
        chainResolver.visit(InputDeclaration.input("region", TypeIdentifier.type("string")));
    }

}
