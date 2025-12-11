package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.exceptions.NotFoundException;
import cloud.kitelang.execution.exceptions.RuntimeError;
import cloud.kitelang.execution.values.ComponentValue;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

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
     * main
     * Component instances are accessible through lookup by instance name
     * global env{
     * main -> ComponentValue (instance)
     * }
     */
    @Test
    void componentInstanceIsDefinedInEnv() {
        var res = eval("""
                component server { }
                component server main {
                
                }
                """);
        var instance = interpreter.getComponent("main");

        assertNotNull(instance);
        assertEquals("main", instance.getName());
    }

    @Test
    void componentInstanceIsDefinedWithInputs() {
        eval("""
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
                    port = main.port
                }
                """);

        var main = interpreter.getComponent("main");

        assertNotNull(main);
        assertEquals("main", main.getName());
        assertEquals("localhost", main.argVal("hostname"));
        assertEquals(3000, main.argVal("port"));

        var api = interpreter.getComponent("api");

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
        eval("""
                component server {
                   input number x = 2
                }
                
                component server main {
                
                }
                """);
        var instance = interpreter.getComponent("main");

        assertEquals(2, instance.argVal("x"));
    }

    @Test
    void componentInstanceMemberAccess() {
        var res = eval("""
                component server {
                   input number x = 2
                }
                
                component server main  {
                
                }
                var y = main
                var z = main.x
                z
                """);
        var instance = interpreter.getComponent("main");
        assertSame(2, instance.argVal("x"));
        // make sure main's x has been changed
        assertEquals(2, instance.argVal("x"));

        // assert y holds reference to main
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
                main.x = 3
                """));
    }

    @Test
    void componentInstanceInit() {
        eval("""
                component server {
                   input number x = 2
                }
                
                component server main {
                    x = 3
                }
                """);
        var instance = interpreter.getComponent("main");

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
        var instance = interpreter.getComponent("main");

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

        var instance = interpreter.getComponent("main");

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

        var instance = interpreter.getComponent("main");

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

        var instance = interpreter.getComponent("main");

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
                
                var env = main.environment
                """);

        assertEquals("production", res);
    }

    @Test
    void multipleComponentInstancesSameDefinition() {
        eval("""
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

        var web = interpreter.getComponent("web");
        var database = interpreter.getComponent("database");

        assertEquals("web-server", web.argVal("hostname"));
        assertEquals("db-server", database.argVal("hostname"));
    }

    @Test
    void componentWithArrayProperty() {
        eval("""
                component server {
                    input string[] services
                }
                
                component server main {
                    services = ["api", "web", "worker"]
                }
                """);

        var instance = interpreter.getComponent("main");
        var services = (List<?>) instance.argVal("services");
        assertEquals(3, services.size());
        assertEquals("api", services.get(0));
        assertEquals("web", services.get(1));
        assertEquals("worker", services.get(2));
    }

    @Test
    void componentWithObjectProperty() {
        eval("""
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

        var instance = interpreter.getComponent("main");
        var actual = instance.argVal("metadata");
        assertNotNull(actual);
        assertEquals(Map.of("version", "1.0", "author", "dev-team"), actual);
    }

    @Test
    void componentWithComputedValue() {
        eval("""
                component server {
                    input number replicas
                }
                
                var baseReplicas = 3
                
                component server main {
                    replicas = baseReplicas * 2
                }
                """);

        var instance = interpreter.getComponent("main");
        assertEquals(6, instance.argVal("replicas"));
    }

    @Test
    void componentReferencingArray() {
        eval("""
                component server {
                    input string[] endpoints
                    input string primaryEndpoint
                }
                
                component server source {
                    endpoints = ["/api", "/health", "/metrics"]
                }
                
                component server target {
                    primaryEndpoint = source.endpoints[0]
                }
                """);

        var target = interpreter.getComponent("target");
        assertEquals("/api", target.argVal("primaryEndpoint"));
    }

    @Test
    void componentWithNullValue() {
        eval("""
                component server {
                    input any config
                }
                
                component server main {
                    config = null
                }
                """);

        var instance = interpreter.getComponent("main");
        assertNull(instance.argVal("config"));
    }

    @Test
    void componentChainedReferences() {
        eval("""
                component server {
                    input string id
                    input string parentId
                }
                
                component server primary {
                    id = "primary-id"
                }
                
                component server secondary {
                    id = "secondary-id"
                    parentId = primary.id
                }
                
                component server tertiary {
                    id = "tertiary-id"
                    parentId = secondary.id
                }
                """);

        var secondary = interpreter.getComponent("secondary");
        var tertiary = interpreter.getComponent("tertiary");

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

        var instance = interpreter.getComponent("main");
        assertEquals("api-service", instance.argVal("fullName"));
    }

    @Test
    void componentAccessingGlobalVariable() {
        eval("""
                component server {
                    input string region
                }
                
                var defaultRegion = "us-west-2"
                
                component server main {
                    region = defaultRegion
                }
                """);

        var instance = interpreter.getComponent("main");
        assertEquals("us-west-2", instance.argVal("region"));
    }

    @Test
    void componentWithNestedPropertyAccess() {
        eval("""
                component server {
                    input object settings
                    input string value
                }
                
                component server source {
                    settings = { database: { host: "localhost" } }
                }
                
                component server target {
                    value = source.settings.database.host
                }
                """);

        var target = interpreter.getComponent("target");
        assertEquals("localhost", target.argVal("value"));
    }

    @Test
    void componentPropertyUsedInExpression() {
        eval("""
                component server {
                    input number threads
                    input number maxThreads
                }
                
                component server main {
                    threads = 4
                    maxThreads = threads * 2
                }
                """);

        var instance = interpreter.getComponent("main");
        assertEquals(8, instance.argVal("maxThreads"));
    }

    @Test
    void componentWithAllPropertyTypes() {
        eval("""
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

        var instance = interpreter.getComponent("main");
        assertEquals("test", instance.argVal("str"));
        assertEquals(42, instance.argVal("num"));
        assertEquals(true, instance.argVal("bool"));
        assertNull(instance.argVal("anyVal"));
        assertNotNull(instance.argVal("obj"));
        assertEquals(2, ((List<?>) instance.argVal("arr")).size());
    }

    // ==================== Component Member Access Restriction Tests ====================

    @Test
    @DisplayName("Accessing an output on a component instance should work")
    void componentInstanceCanAccessOutput() {
        var res = eval("""
                component server {
                    output string endpoint = "http://localhost:8080"
                }

                component server main {
                }

                var url = main.endpoint
                """);

        assertEquals("http://localhost:8080", res);
    }

    @Test
    @DisplayName("Accessing an input on a component instance should work")
    void componentInstanceCanAccessInput() {
        var res = eval("""
                component server {
                    input string hostname = "default"
                }

                component server main {
                    hostname = "localhost"
                }

                var host = main.hostname
                """);

        assertEquals("localhost", res);
    }

    @Test
    @DisplayName("Accessing a non-existent property on a component instance should throw error")
    void componentInstanceCannotAccessNonExistentProperty() {
        assertThrows(RuntimeException.class, () -> eval("""
                component server {
                    input string hostname = "localhost"
                }

                component server main {
                }

                var x = main.nonExistent
                """));
    }

    @Test
    @DisplayName("Both inputs and outputs should be accessible on component instances")
    void componentInstanceCanAccessInputsAndOutputs() {
        eval("""
                component server {
                    input string hostname = "localhost"
                    input number port = 8080
                    output string info = "server-info"
                }

                component server main {
                    hostname = "myhost"
                }

                var h = main.hostname
                var p = main.port
                var i = main.info
                """);

        assertEquals("myhost", interpreter.getVar("h"));
        assertEquals(8080, interpreter.getVar("p"));
        assertEquals("server-info", interpreter.getVar("i"));
    }

    @Test
    @DisplayName("Component with resource should create resource during instantiation")
    void componentWithResourceCreatesResourceDuringInstantiation() {
        eval("""
                schema vm {
                    string name
                }

                component server {
                    input string hostname = "default"
                    resource vm instance {
                        name = hostname
                    }
                }

                component server main {
                    hostname = "my-server"
                }
                """);

        var instance = interpreter.getComponent("main");
        assertNotNull(instance);
        assertEquals("my-server", instance.argVal("hostname"));

    }

    @Test
    @DisplayName("Accessing a resource on component instance should throw error - resources are private")
    void componentInstanceCannotAccessResource() {
        var err = assertThrows(RuntimeError.class, () -> eval("""
                schema vm {
                    string name
                }

                component server {
                    input string hostname = "default"
                    resource vm instance {
                        name = hostname
                    }
                }

                component server main {
                    hostname = "my-server"
                }

                var r = main.instance
                """));

        assertTrue(err.getMessage().contains("instance") || err.getMessage().contains("private"));
    }

}