package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Expressions.InputDeclaration;
import io.kite.Runtime.Inputs.InputChainResolver;
import io.kite.Runtime.Inputs.CliResolver;
import io.kite.Runtime.Inputs.EnvResolver;
import io.kite.Runtime.Inputs.InputsFilesResolver;
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
public class InputInputChainResolverTest extends RuntimeTest {
    private InputChainResolver inputChainResolver;
    private Map<String, Object> envVariables;

    @BeforeEach
    void initChain() {
        envVariables = new HashMap<>();
        inputChainResolver = new InputChainResolver(List.of(new EnvResolver(envVariables), new InputsFilesResolver(), new CliResolver()));
        InputsFilesResolver.deleteDefaults();
    }

    @Test
    @DisplayName("Env+File skipped throws on last step")
    void testThrowsOnLastStepCLI() {
        Assertions.assertThrows(MissingInputException.class, () -> inputChainResolver.visit(InputDeclaration.input("client", TypeIdentifier.type("string"))));
    }

    @Test
    @DisplayName("Test Env is present and rest are absent")
    void testEnvPresentFileAndCliAbsent() {
        envVariables.put("client", "env");
        var res = inputChainResolver.visit(InputDeclaration.input("client", TypeIdentifier.type("string")));
        Assertions.assertEquals("env", res);
    }

    @Test
    @DisplayName("Test Env and File is present and Cli absent. File overrides env")
    void testEnvAndFilePresentButCliAbsent() {
        envVariables.put("client", "env");
        InputsFilesResolver.writeToDefaults(Map.of("client", "file"));

        var res = inputChainResolver.visit(InputDeclaration.input("client", TypeIdentifier.type("string")));
        Assertions.assertEquals("file", res);
    }

    @Test
    @DisplayName("Test File is present and Env Cli absent. File overrides env")
    void testFilePresentButEnvCliAbsent() {
        InputsFilesResolver.writeToDefaults(Map.of("client", "file"));

        var res = inputChainResolver.visit(InputDeclaration.input("client", TypeIdentifier.type("string")));
        Assertions.assertEquals("file", res);
    }

}
