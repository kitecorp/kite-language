package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Runtime.Inputs.ChainResolver;
import io.kite.Runtime.Inputs.CliResolver;
import io.kite.Runtime.Inputs.EnvResolver;
import io.kite.Runtime.Inputs.InputsDefaultsFilesFinder;
import io.kite.Runtime.exceptions.MissingInputException;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
public class InputChainResolverTest extends RuntimeTest {
    private ChainResolver chainResolver;
    private Map<String, String> envVariables;

    @BeforeEach
    void initChain() {
        envVariables = new HashMap<>();
        chainResolver = new ChainResolver(List.of(new EnvResolver(envVariables), new InputsDefaultsFilesFinder(), new CliResolver()));
    }

    @Test
    @DisplayName("Env+File skipped throws on last step")
    void testThrowsOnLastStepCLI() {
        Assertions.assertThrows(MissingInputException.class, () -> chainResolver.visit(InputDeclaration.input("client", TypeIdentifier.type("string"))));
    }

    @Test
    @DisplayName("Test Env is present and rest are absent")
    void testEnvPresentFileAndCliAbsent() {
        envVariables.put("client", "test");
        var res = chainResolver.visit(InputDeclaration.input("client", TypeIdentifier.type("string")));
        Assertions.assertEquals("test", res);
    }

}
