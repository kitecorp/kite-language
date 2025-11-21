package io.kite.runtime;

import io.kite.base.RuntimeTest;
import io.kite.runtime.exceptions.NotFoundException;
import io.kite.runtime.exceptions.RuntimeError;
import io.kite.runtime.values.ComponentValue;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class ComponentTest extends RuntimeTest {
    @Test
    void componentDefinitionCreatedInGlobalEnv() {
        var x = eval("""
                component server { }
                """);
        var fromEnv = interpreter.getEnv().lookup("server");
        assertSame(x, fromEnv);
    }

    @Test
    void duplicateComponentDefinitionThrowsError() {
        var err = assertThrows(RuntimeException.class, () -> eval("""
                component server { }
                component server {
                
                }
                """));
        assertEquals("Duplicate component definition: server", err.getMessage());
    }

    /**
     * This checks for the following syntax
     * server.main
     * Component instances are accessible through lookup
     * global env{
     * server.main -> ComponentValue (instance)
     * }
     */
    @Test
    void componentInstanceIsDefinedInEnv() {
        var res = eval("""
                component server { }
                component server main {
                
                }
                """);
        log.warn((res));
        var instance = interpreter.getComponent("server.main");

        assertNotNull(instance);
        assertEquals("main", instance.getName());
    }

    @Test
    void componentInstanceIsDefinedWithInputs() {
        var res = eval("""
                component server { 
                    input string hostname
                    input number port = 8080
                }
                component server main {
                    hostname = "localhost"
                    port = 3000
                }
                component server api {
                    hostname = "api.example.com"
                    port = server.main.port
                }
                """);
        log.warn((res));

        var main = interpreter.getComponent("server.main");

        assertNotNull(main);
        assertEquals("main", main.getName());
        assertEquals("localhost", main.argVal("hostname"));
        assertEquals(3000, main.argVal("port"));

        var api = interpreter.getComponent("server.api");

        assertNotNull(api);
        assertEquals("api", api.getName());
        assertEquals("api.example.com", api.argVal("hostname"));
        assertEquals(3000, api.argVal("port"));
    }


    @Test
    @DisplayName("throw if a component instance uses an input not defined in the component definition")
    void componentInstanceThrowsIfInputNotDefinedInDefinition() {
        assertThrows(NotFoundException.class, () -> eval("""
                component server {
                }
                
                component server main {
                    x = 3
                }
                """));
    }


    @Test
    void componentInstanceInheritsDefaultInputValue() {
        var res = eval("""
                component server {
                   input number x = 2
                }
                
                component server main {
                
                }
                """);
        log.warn(res);
        var instance = interpreter.getComponent("server.main");

        assertEquals(2, instance.getProperties().lookup("x"));
    }

    @Test
    @Disabled
    void componentInstanceInheritsDefaultInputValueVal() {
        var res = eval("""
                component server {
                   input number x = 2
                }
                
                component server main {
                
                }
                """);
        log.warn((res));
        var instance = interpreter.getComponent("server.main");

        assertEquals(2, instance.getProperties().lookup("x"));
    }

    @Test
    void componentInstanceMemberAccess() {
        var res = eval("""
                component server {
                   input number x = 2
                }
                
                component server main  {
                
                }
                var y = server.main
                var z = server.main.x
                z
                """);
        log.warn((res));
        var instance = interpreter.getComponent("server.main");
        assertSame(2, instance.getProperties().get("x"));
        // make sure main's x has been changed
        assertEquals(2, instance.getProperties().get("x"));

        // assert y holds reference to server.main
        var y = interpreter.getVar("y");
        assertSame(y, instance);
        // assert z holds reference to the value of x (which is 2)
        var z = interpreter.getVar("z");
        assertEquals(2, z);

        assertEquals(2, res);
    }

    /**
     * Change a value in the component instance works
     * It should not change the component definition default values
     * It should only change the member of the instance
     */
    @Test
    void componentInstanceSetMemberAccess() {
        assertThrows(RuntimeError.class, () -> eval("""
                component server {
                   input number x = 2
                }
                
                component server main {
                
                }
                server.main.x = 3
                """));
    }

    @Test
    void componentInstanceInit() {
        var res = eval("""
                component server {
                   input number x = 2
                }
                
                component server main {
                    x = 3
                }
                """);
        log.warn((res));
        var instance = interpreter.getComponent("server.main");

        // x of main instance was updated with a new value
        var x = instance.get("x");
        assertEquals(3, x);
    }

    @SneakyThrows
    @Test
    void componentInstanceInitJson() {
        var res = eval("""
                component server {
                   input number x = 2
                }
                
                component server main  {
                    x = 3
                }
                """);
        log.warn((res));
        var instance = interpreter.getComponent("server.main");

        assertInstanceOf(ComponentValue.class, instance);
    }

    @Test
    @DisplayName("Resolve var name using double quotes string interpolation inside if statement")
    void testInterpolation() {
        var res = eval("""
                component server {
                   input string environment
                }
                var env = 'production'
                component server main {
                  environment = "$env"
                }
                """);

        var instance = interpreter.getComponent("server.main");

        assertInstanceOf(ComponentValue.class, instance);
        assertEquals(instance, res);
        assertEquals("production", instance.argVal("environment"));
    }

    @Test
    @DisplayName("Resolve var name using double quotes string interpolation inside if statement")
    void testInterpolationSingleQuotes() {
        var res = eval("""
                component server {
                   input string environment
                }
                var env = 'production'
                component server main {
                  environment = '$env'
                }
                """);

        var instance = interpreter.getComponent("server.main");

        assertInstanceOf(ComponentValue.class, instance);
        assertEquals(instance, res);
        assertEquals("$env", instance.argVal("environment"));
    }

    @Test
    @DisplayName("Resolve var name using double quotes string interpolation with braces")
    void testInterpolationWithBraces() {
        var res = eval("""
                component server {
                   input string environment
                }
                var env = 'production'
                component server main {
                  environment = "${env}"
                }
                """);

        var instance = interpreter.getComponent("server.main");

        assertInstanceOf(ComponentValue.class, instance);
        assertEquals(instance, res);
        assertEquals("production", instance.argVal("environment"));
    }

    @Test
    void testAccessComponentInstanceProperty() {
        var res = eval("""
                component server {
                   input string environment
                }
                
                component server main {
                  environment = 'production'
                }
                
                var env = server.main.environment
                """);

        assertEquals("production", res);
    }

    @Test
    void multipleComponentInstancesSameDefinition() {
        var res = eval("""
                component server {
                    input string hostname
                }
                
                component server web {
                    hostname = "web-server"
                }
                
                component server database {
                    hostname = "db-server"
                }
                """);

        var web = interpreter.getComponent("server.web");
        var database = interpreter.getComponent("server.database");

        assertEquals("web-server", web.argVal("hostname"));
        assertEquals("db-server", database.argVal("hostname"));
    }

    @Test
    void componentWithArrayProperty() {
        var res = eval("""
                component server {
                    input string[] services
                }
                
                component server main {
                    services = ["api", "web", "worker"]
                }
                """);

        var instance = interpreter.getComponent("server.main");
        var services = (List<?>) instance.argVal("services");
        assertEquals(3, services.size());
        assertEquals("api", services.get(0));
    }

    @Test
    void componentWithObjectProperty() {
        var res = eval("""
                component server {
                    input object metadata
                }
                
                component server main {
                    metadata = {
                        version: "1.0",
                        author: "dev-team"
                    }
                }
                """);

        var instance = interpreter.getComponent("server.main");
        assertNotNull(instance.argVal("metadata"));
    }

    @Test
    void componentWithComputedValue() {
        var res = eval("""
                component server {
                    input number replicas
                }
                
                var baseReplicas = 3
                
                component server main {
                    replicas = baseReplicas * 2
                }
                """);

        var instance = interpreter.getComponent("server.main");
        assertEquals(6, instance.argVal("replicas"));
    }

    @Test
    void componentReferencingArray() {
        var res = eval("""
                component server {
                    input string[] endpoints
                    input string primaryEndpoint
                }
                
                component server source {
                    endpoints = ["/api", "/health", "/metrics"]
                }
                
                component server target {
                    primaryEndpoint = server.source.endpoints[0]
                }
                """);

        var target = interpreter.getComponent("server.target");
        assertEquals("/api", target.argVal("primaryEndpoint"));
    }

    @Test
    void componentWithNullValue() {
        var res = eval("""
                component server {
                    input any config
                }
                
                component server main {
                    config = null
                }
                """);

        var instance = interpreter.getComponent("server.main");
        assertNull(instance.argVal("config"));
    }

    @Test
    void componentChainedReferences() {
        var res = eval("""
                component server {
                    input string id
                    input string parentId
                }
                
                component server primary {
                    id = "primary-id"
                }
                
                component server secondary {
                    id = "secondary-id"
                    parentId = server.primary.id
                }
                
                component server tertiary {
                    id = "tertiary-id"
                    parentId = server.secondary.id
                }
                """);

        var secondary = interpreter.getComponent("server.secondary");
        var tertiary = interpreter.getComponent("server.tertiary");

        assertEquals("primary-id", secondary.argVal("parentId"));
        assertEquals("secondary-id", tertiary.argVal("parentId"));
    }

    @Test
    void componentWithStringConcatenation() {
        var res = eval("""
                component server {
                    input string name
                    input string fullName
                }
                
                component server main {
                    name = "api"
                    fullName = name + "-service"
                }
                """);

        var instance = interpreter.getComponent("server.main");
        assertEquals("api-service", instance.argVal("fullName"));
    }

    @Test
    void componentAccessingGlobalVariable() {
        var res = eval("""
                component server {
                    input string region
                }
                
                var defaultRegion = "us-west-2"
                
                component server main {
                    region = defaultRegion
                }
                """);

        var instance = interpreter.getComponent("server.main");
        assertEquals("us-west-2", instance.argVal("region"));
    }

    @Test
    void componentWithNestedPropertyAccess() {
        var res = eval("""
                component server {
                    input object settings
                    input string value
                }
                
                component server source {
                    settings = { database: { host: "localhost" } }
                }
                
                component server target {
                    value = server.source.settings.database.host
                }
                """);

        var target = interpreter.getComponent("server.target");
        assertEquals("localhost", target.argVal("value"));
    }

    @Test
    void componentPropertyUsedInExpression() {
        var res = eval("""
                component server {
                    input number threads
                    input number maxThreads
                }
                
                component server main {
                    threads = 4
                    maxThreads = threads * 2
                }
                """);

        var instance = interpreter.getComponent("server.main");
        assertEquals(8, instance.argVal("maxThreads"));
    }

    @Test
    void componentWithAllPropertyTypes() {
        var res = eval("""
                component server {
                    input string str
                    input number num
                    input boolean bool
                    input any anyVal
                    input object obj
                    input string[] arr
                }
                
                component server main {
                    str = "test"
                    num = 42
                    bool = true
                    anyVal = null
                    obj = {key: "value"}
                    arr = ["a", "b"]
                }
                """);

        var instance = interpreter.getComponent("server.main");
        assertEquals("test", instance.argVal("str"));
        assertEquals(42, instance.argVal("num"));
        assertEquals(true, instance.argVal("bool"));
        assertNull(instance.argVal("anyVal"));
        assertNotNull(instance.argVal("obj"));
        assertEquals(2, ((List<?>) instance.argVal("arr")).size());
    }

}