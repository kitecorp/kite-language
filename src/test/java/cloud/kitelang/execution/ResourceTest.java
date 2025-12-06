package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.exceptions.NotFoundException;
import cloud.kitelang.execution.exceptions.RuntimeError;
import cloud.kitelang.execution.values.ResourceValue;
import cloud.kitelang.syntax.ast.ValidationException;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class ResourceTest extends RuntimeTest {
    @Test
    void newResourceThrowsIfNoNameIsSpecified() {
        assertThrows(ValidationException.class, () -> eval("""
                schema vm { }
                resource vm {
                
                }
                """));
    }

    /**
     * This checks for the following syntax
     * main (direct access)
     * All resources are defined in the environment where they're created
     * global env{
     * main -> resource vm
     * }
     */
    @Test
    void resourceIsDefinedInSchemaEnv() {
        var res = eval("""
                schema vm { }
                resource vm main {
                
                }
                """);
        var resource = interpreter.getInstance("main");

        assertNotNull(resource);
        assertEquals("main", resource.getName());
    }

    @Test
    void resourceIsDefinedInSchema() {
        var res = eval("""
                schema vm {
                    string name
                    number maxCount=0
                }
                resource vm main {
                    name = "first"
                    maxCount=1
                }
                resource vm second {
                    name = "second"
                    maxCount = main.maxCount
                }
                """);
        var resource = interpreter.getInstance("main");

        assertNotNull(resource);
        assertEquals("main", resource.getName());
        assertEquals("first", resource.argVal("name"));
        assertEquals(1, resource.argVal("maxCount"));

        var second = interpreter.getInstance("second");

        assertNotNull(second);
        assertEquals("second", second.getName());
        assertEquals("second", second.argVal("name"));
        assertEquals(1, second.argVal("maxCount"));
    }


    @Test
    @DisplayName("throw if a resource uses a field not defined in the schema")
    void resourceThrowsIfFieldNotDefinedInSchema() {
        assertThrows(NotFoundException.class, () -> eval("""
                schema vm {
                }
                
                resource vm main {
                    x = 3
                }
                """));
    }


    @Test
    void resourceInheritsDefaultSchemaValue() {
        var res = eval("""
                schema vm {
                   number x = 2
                }
                
                resource vm main {
                
                }
                """);
        log.warn(res);
        var resource = interpreter.getInstance("main");

        assertEquals(2, resource.getProperties().lookup("x"));
    }

    @Test
    @Disabled
    void resourceInheritsDefaultSchemaValueVal() {
        var res = eval("""
                schema vm {
                   number x = 2
                }
                
                resource vm main {
                
                }
                """);
        var schema = interpreter.getSchema("vm");

        var resource = interpreter.getInstance("main");

        assertEquals(2, resource.getProperties().lookup("x"));
    }

    @Test
    void resourceMemberAccess() {
        var res = eval("""
                schema vm {
                   number x = 2
                }
                
                resource vm main  {
                
                }
                var y = main
                var z = main.x
                z
                """);
        var resource = interpreter.getInstance("main");
        assertSame(2, resource.getProperties().get("x"));
        // make sure main's x has been changed
        assertEquals(2, resource.getProperties().get("x"));

        // assert y holds reference to main
        var y = interpreter.getVar("y");
        assertSame(y, resource);
        // assert z holds reference to main.x
        var z = interpreter.getVar("z");
        assertEquals(2, z);

        assertEquals(2, res);
    }

    /**
     * Change a value in the resource instance works
     * It should not change the schema default values
     * It should only change the member of the resource
     */
    @Test
    void resourceSetMemberAccess() {
        assertThrows(RuntimeError.class, () -> eval("""
                schema vm {
                   number x = 2
                }
                
                resource vm  main {
                
                }
                main.x = 3
                """));
    }

    @Test
    void resourceInit() {
        var res = eval("""
                schema vm {
                   number x = 2
                }
                
                resource vm main {
                    x = 3
                }
                """);
        var schema = interpreter.getSchema("vm");
        var resource = interpreter.getInstance("main");

        // default x in schema remains the same
        assertEquals(2, schema.getEnvironment().get("x"));

        // x of main resource was updated with a new value
        var x = resource.get("x");
        assertEquals(3, x);
    }

    @SneakyThrows
    @Test
    void resourceInitJson() {
        var res = eval("""
                schema vm {
                   number x = 2
                }
                
                resource vm main  {
                    x = 3
                }
                """);
        var resource = interpreter.getInstance("main");

        assertInstanceOf(ResourceValue.class, resource);
    }

    @Test
    @DisplayName("Resolve var name using double quotes string interpolation inside if statement")
    void testInterpolation() {
        var res = eval("""
                schema vm {
                   string name
                }
                var name = 'prod'
                resource vm main {
                  name     = "$name"
                }
                """);

        var resource = interpreter.getInstance("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
        assertEquals("prod", resource.argVal("name"));
    }

    @Test
    @DisplayName("Resolve var name interpolation inside resource statement")
    void testInterpolationSingleQuotes() {
        var res = eval("""
                schema vm {
                   string name
                }
                var name = 'prod'
                resource vm main {
                  name     = "$name"
                }
                """);

        var resource = interpreter.getInstance("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
        assertEquals("prod", resource.argVal("name"));
    }

    @Test
    @DisplayName("Resolve var name string interpolation inside resource")
    void testInterpolationSingleQuotesCurlyBraces() {
        var res = eval("""
                schema vm {
                   string name
                }
                var name = 'prod'
                resource vm main {
                  name     = "${name}"
                }
                """);

        var resource = interpreter.getInstance("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
        assertEquals("prod", resource.argVal("name"));
    }

    @Test
    void testAccessResourceProperty() {
        var res = eval("""
                schema vm {
                   string name
                }
                
                resource vm main {
                  name     = 'prod'
                }
                
                var name = main.name
                """);

        assertEquals("prod", res);
    }

    @Test
    void multipleResourcesSameSchema() {
        var res = eval("""
                schema vm {
                    string name
                }
                
                resource vm web {
                    name = "web-server"
                }
                
                resource vm db {
                    name = "database"
                }
                """);

        var web = interpreter.getInstance("web");
        var db = interpreter.getInstance("db");
        assertEquals("web-server", web.argVal("name"));
        assertEquals("database", db.argVal("name"));
    }

    @Test
    void resourceWithArrayProperty() {
        var res = eval("""
                schema vm {
                    string[] tags
                }
                
                resource vm main {
                    tags = ["production", "critical"]
                }
                """);

        var resource = interpreter.getInstance("main");
        var tags = (List<?>) resource.argVal("tags");
        assertEquals(2, tags.size());
        assertEquals("production", tags.get(0));
    }

    @Test
    void resourceWithObjectProperty() {
        var res = eval("""
                schema vm {
                    object config
                }
                
                resource vm main {
                    config = {
                        env: "prod",
                        region: "us-east"
                    }
                }
                """);

        var resource = interpreter.getInstance("main");
        assertNotNull(resource.argVal("config"));
    }

    @Test
    void resourceWithComputedValue() {
        var res = eval("""
                schema vm {
                    number count
                }
                
                var baseCount = 5
                
                resource vm main {
                    count = baseCount * 2
                }
                """);

        var resource = interpreter.getInstance("main");
        assertEquals(10, resource.argVal("count"));
    }

    @Test
    void resourceReferencingArray() {
        var res = eval("""
                schema vm {
                    string[] names
                    string selectedName
                }
                
                resource vm source {
                    names = ["first", "second", "third"]
                }
                
                resource vm target {
                    selectedName = source.names[0]
                }
                """);

        var target = interpreter.getInstance("target");
        assertEquals("first", target.argVal("selectedName"));
    }

    @Test
    void resourceWithNullValue() {
        var res = eval("""
                schema vm {
                    any value
                }
                
                resource vm main {
                    value = null
                }
                """);

        var resource = interpreter.getInstance("main");
        assertNull(resource.argVal("value"));
    }

    @Test
    void resourceChainedReferences() {
        var res = eval("""
                schema vm {
                    string id
                    string refId
                }
                
                resource vm first {
                    id = "first-id"
                }
                
                resource vm second {
                    id = "second-id"
                    refId = first.id
                }
                
                resource vm third {
                    id = "third-id"
                    refId = second.id
                }
                """);

        var second = interpreter.getInstance("second");
        var third = interpreter.getInstance("third");
        assertEquals("first-id", second.argVal("refId"));
        assertEquals("second-id", third.argVal("refId"));
    }

    @Test
    void resourceWithStringConcatenation() {
        var res = eval("""
                schema vm {
                    string name
                    string fullName
                }
                
                resource vm main {
                    name = "server"
                    fullName = name + "-production"
                }
                """);

        var resource = interpreter.getInstance("main");
        assertEquals("server-production", resource.argVal("fullName"));
    }

    @Test
    void resourceAccessingGlobalVariable() {
        var res = eval("""
                schema vm {
                    string region
                }
                
                var defaultRegion = "us-east-1"
                
                resource vm main {
                    region = defaultRegion
                }
                """);

        var resource = interpreter.getInstance("main");
        assertEquals("us-east-1", resource.argVal("region"));
    }

    @Test
    void resourceWithNestedPropertyAccess() {
        var res = eval("""
                schema vm {
                    object config
                    string value
                }
                
                resource vm source {
                    config = { nested: { key: "value" } }
                }
                
                resource vm target {
                    value = source.config.nested.key
                }
                """);

        var target = interpreter.getInstance("target");
        assertEquals("value", target.argVal("value"));
    }

    @Test
    void resourcePropertyUsedInExpression() {
        var res = eval("""
                schema vm {
                    number count
                    number doubled
                }
                
                resource vm main {
                    count = 5
                    doubled = count * 2
                }
                """);

        var resource = interpreter.getInstance("main");
        assertEquals(10, resource.argVal("doubled"));
    }

    @Test
    void resourceWithAllPropertyTypes() {
        var res = eval("""
                schema vm {
                    string str
                    number num
                    boolean bool
                    any anyVal
                    object obj
                    string[] arr
                }
                
                resource vm main {
                    str = "test"
                    num = 42
                    bool = true
                    anyVal = null
                    obj = {key: "value"}
                    arr = ["a", "b"]
                }
                """);

        var resource = interpreter.getInstance("main");
        assertEquals("test", resource.argVal("str"));
        assertEquals(42, resource.argVal("num"));
        assertEquals(true, resource.argVal("bool"));
        assertNull(resource.argVal("anyVal"));
        assertNotNull(resource.argVal("obj"));
        assertEquals(2, ((List<?>) resource.argVal("arr")).size());
    }

}
