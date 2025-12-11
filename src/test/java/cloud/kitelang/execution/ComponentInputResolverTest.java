package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.exceptions.MissingInputException;
import cloud.kitelang.execution.inputs.EnvResolver;
import cloud.kitelang.execution.inputs.InputChainResolver;
import cloud.kitelang.execution.inputs.InputsFilesResolver;
import cloud.kitelang.semantics.TypeChecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for component input resolution via InputChainResolver.
 * Component inputs should be resolvable via qualified names (e.g., "api.hostname").
 */
public class ComponentInputResolverTest extends RuntimeTest {
    private Map<String, Object> envVariables;
    private InputChainResolver inputChainResolver;
    private TypeChecker typeChecker;

    @Override
    protected void init() {
        super.init();
        envVariables = new HashMap<>();
        inputChainResolver = new InputChainResolver(List.of(new EnvResolver(envVariables)));
        typeChecker = new TypeChecker(printer);
    }

    @AfterEach
    void cleanup() {
        InputsFilesResolver.deleteDefaults();
    }

    protected Object eval(String source) {
        program = parse(source);
        scopeResolver.resolve(program);
        inputChainResolver.visit(program);
        // Wire resolved component inputs to interpreter
        interpreter.setResolvedComponentInputs(inputChainResolver.getResolvedComponentInputs());
        typeChecker.visit(program);
        return interpreter.visit(program);
    }

    @Test
    @DisplayName("Component input resolved from env variable with qualified name")
    void componentInputFromEnv() {
        envVariables.put("api.hostname", "env-host.example.com");

        var result = eval("""
                component server {
                    input string hostname
                }
                component server api {
                }
                api.hostname
                """);

        assertEquals("env-host.example.com", result);
    }

    @Test
    @DisplayName("Component input with default value, overridden by env")
    void componentInputDefaultOverriddenByEnv() {
        envVariables.put("api.port", "9090");

        var result = eval("""
                component server {
                    input number port = 8080
                }
                component server api {
                }
                api.port
                """);

        assertEquals(9090, result);
    }

    @Test
    @DisplayName("Component input uses default when no external value provided")
    void componentInputUsesDefault() {
        var result = eval("""
                component server {
                    input string hostname = "localhost"
                }
                component server api {
                }
                api.hostname
                """);

        assertEquals("localhost", result);
    }

    @Test
    @DisplayName("Component input resolved from file with qualified name")
    void componentInputFromFile() {
        InputsFilesResolver.writeToDefaults(Map.of("api.hostname", "file-host.example.com"));
        inputChainResolver = new InputChainResolver(List.of(new InputsFilesResolver()));

        var result = eval("""
                component server {
                    input string hostname
                }
                component server api {
                }
                api.hostname
                """);

        assertEquals("file-host.example.com", result);
    }

    @Test
    @DisplayName("Missing component input throws MissingInputException")
    void missingComponentInputThrows() {
        assertThrows(MissingInputException.class, () -> eval("""
                component server {
                    input string hostname
                }
                component server api {
                }
                api.hostname
                """));
    }

    @Test
    @DisplayName("Multiple component instances with different inputs")
    void multipleComponentInstances() {
        envVariables.put("prod.hostname", "prod.example.com");
        envVariables.put("dev.hostname", "dev.example.com");

        var result = eval("""
                component server {
                    input string hostname
                }
                component server prod {
                }
                component server dev {
                }
                prod.hostname + " - " + dev.hostname
                """);

        assertEquals("prod.example.com - dev.example.com", result);
    }

    @Test
    @DisplayName("Component input with number type from env")
    void componentInputNumberFromEnv() {
        envVariables.put("api.port", "3000");

        var result = eval("""
                component server {
                    input number port
                }
                component server api {
                }
                api.port
                """);

        assertEquals(3000, result);
    }

    @Test
    @DisplayName("Component input with boolean type from env")
    void componentInputBooleanFromEnv() {
        envVariables.put("api.enabled", "true");

        var result = eval("""
                component server {
                    input boolean enabled
                }
                component server api {
                }
                api.enabled
                """);

        assertEquals(true, result);
    }

    @Test
    @DisplayName("Env variable takes precedence over file for component input")
    void envTakesPrecedenceOverFile() {
        InputsFilesResolver.writeToDefaults(Map.of("api.hostname", "file-host"));
        envVariables.put("api.hostname", "env-host");
        inputChainResolver = new InputChainResolver(List.of(
                new InputsFilesResolver(),
                new EnvResolver(envVariables)
        ));

        var result = eval("""
                component server {
                    input string hostname
                }
                component server api {
                }
                api.hostname
                """);

        assertEquals("env-host", result);
    }

    @Test
    @DisplayName("Component definition (no instance name) should not resolve inputs")
    void componentDefinitionDoesNotResolveInputs() {
        // Component definitions don't have instance names, so inputs are not resolved via chain
        // They only get resolved when instantiated
        envVariables.put("hostname", "should-not-be-used");

        var result = eval("""
                component server {
                    input string hostname = "default"
                }
                component server api {
                    hostname = "explicit"
                }
                api.hostname
                """);

        assertEquals("explicit", result);
    }
}
