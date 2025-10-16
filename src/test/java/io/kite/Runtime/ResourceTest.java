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

}
