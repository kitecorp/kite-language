package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.Frontend.Parser.ParserErrors;
import io.kite.Runtime.Values.ResourceValue;
import io.kite.Runtime.Values.SchemaValue;
import io.kite.Runtime.exceptions.NotFoundException;
import io.kite.Runtime.exceptions.RuntimeError;
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
        eval("""
                schema vm { }
                resource vm {
                
                }
                """);
        assertFalse(ParserErrors.getErrors().isEmpty());
    }

    /**
     * This checks for the following syntax
     * vm.main
     * All resources are defined in the schema
     * global env{
     * vm   SchemaValue -> variables{ main -> resource vm}
     * }
     */
    @Test
    void resourceIsDefinedInSchemaEnv() {
        var res = eval("""
                schema vm { }
                resource vm main {
                
                }
                """);
        log.warn((res));
        var schema = (SchemaValue) global.get("vm");

        assertNotNull(schema);
        assertEquals("vm", schema.getType());


        var resource = schema.findInstance("main");

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
                    maxCount = vm.main.maxCount
                }
                """);
        log.warn((res));
        var schema = (SchemaValue) global.get("vm");

        assertNotNull(schema);
        assertEquals("vm", schema.getType());


        var resource = schema.findInstance("main");

        assertNotNull(resource);
        assertEquals("main", resource.getName());
        assertEquals("first", resource.argVal("name"));
        assertEquals(1, resource.argVal("maxCount"));

        var second = schema.findInstance("second");

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
        var schema = (SchemaValue) global.get("vm");

        var resource = schema.findInstance("main");

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
        log.warn((res));
        var schema = (SchemaValue) global.get("vm");

        var resource = schema.findInstance("main");

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
                var y = vm.main
                var z = vm.main.x
                z
                """);
        log.warn((res));
        var schema = (SchemaValue) global.get("vm");

        var resource = schema.findInstance("main");
        assertSame(2, resource.getProperties().get("x"));
        // make sure main's x has been changed
        assertEquals(2, resource.getProperties().get("x"));

        // assert y holds reference to vm.main
        var y = global.lookup("y");
        assertSame(y, resource);
        // assert y holds reference to vm.main
        var z = global.lookup("z");
        assertSame(z, schema.getEnvironment().get("x"));

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
                vm.main.x = 3
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
        log.warn((res));
        var schema = (SchemaValue) global.get("vm");

        var resource = schema.findInstance("main");

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
        log.warn((res));
        var schema = (SchemaValue) global.get("vm");

        var resource = schema.findInstance("main");

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

        var schema = (SchemaValue) global.get("vm");

        var resource = schema.findInstance("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
        assertEquals("prod", resource.argVal("name"));
    }

    @Test
    @DisplayName("Resolve var name using double quotes string interpolation inside if statement")
    void testInterpolationSingleQuotes() {
        var res = eval("""
                schema vm {
                   string name
                }
                var name = 'prod'
                resource vm main {
                  name     = '$name'
                }
                """);

        var schema = (SchemaValue) global.get("vm");

        var resource = schema.findInstance("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
        assertEquals("prod", resource.argVal("name"));
    }

    @Test
    @DisplayName("Resolve var name using double quotes string interpolation inside if statement")
    void testInterpolationSingleQuotesCurlyBraces() {
        var res = eval("""
                schema vm {
                   string name
                }
                var name = 'prod'
                resource vm main {
                  name     = '${name}'
                }
                """);

        var schema = (SchemaValue) global.get("vm");

        var resource = schema.findInstance("main");

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
                
                var name = vm.main.name
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

        var schema = (SchemaValue) global.get("vm");
        assertEquals("web-server", schema.findInstance("web").argVal("name"));
        assertEquals("database", schema.findInstance("db").argVal("name"));
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

        var schema = (SchemaValue) global.get("vm");
        var resource = schema.findInstance("main");
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

        var schema = (SchemaValue) global.get("vm");
        var resource = schema.findInstance("main");
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

        var schema = (SchemaValue) global.get("vm");
        var resource = schema.findInstance("main");
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
                    selectedName = vm.source.names[0]
                }
                """);

        var schema = (SchemaValue) global.get("vm");
        var target = schema.findInstance("target");
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

        var schema = (SchemaValue) global.get("vm");
        var resource = schema.findInstance("main");
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
                    refId = vm.first.id
                }
                
                resource vm third {
                    id = "third-id"
                    refId = vm.second.id
                }
                """);

        var schema = (SchemaValue) global.get("vm");
        assertEquals("first-id", schema.findInstance("second").argVal("refId"));
        assertEquals("second-id", schema.findInstance("third").argVal("refId"));
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

        var schema = (SchemaValue) global.get("vm");
        var resource = schema.findInstance("main");
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

        var schema = (SchemaValue) global.get("vm");
        var resource = schema.findInstance("main");
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
                    value = vm.source.config.nested.key
                }
                """);

        var schema = (SchemaValue) global.get("vm");
        var target = schema.findInstance("target");
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

        var schema = (SchemaValue) global.get("vm");
        var resource = schema.findInstance("main");
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

        var schema = (SchemaValue) global.get("vm");
        var resource = schema.findInstance("main");
        assertEquals("test", resource.argVal("str"));
        assertEquals(42, resource.argVal("num"));
        assertEquals(true, resource.argVal("bool"));
        assertNull(resource.argVal("anyVal"));
        assertNotNull(resource.argVal("obj"));
        assertEquals(2, ((List<?>) resource.argVal("arr")).size());
    }

}
