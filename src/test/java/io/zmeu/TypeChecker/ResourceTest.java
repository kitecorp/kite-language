package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.ParserErrors;
import io.kite.Runtime.exceptions.NotFoundException;
import io.kite.TypeChecker.Types.ArrayType;
import io.kite.TypeChecker.Types.ResourceType;
import io.kite.TypeChecker.Types.SchemaType;
import io.kite.TypeChecker.Types.ValueType;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class ResourceTest extends CheckerTest {
    @Test
    void newResourceThrowsIfNoNameIsSpecified() {
        eval("""
                schema Server { }
                resource Server {
                
                }
                """);
        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
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
        ResourceType res = (ResourceType) eval("""
                schema Server { }
                resource Server main  {
                
                }
                """);
        var schema = (SchemaType) checker.getEnv().get("Server");

        assertNotNull(schema);
        assertEquals(SchemaType.class, schema.getClass());
        assertEquals("Server", schema.getValue());

        assertNotNull(res);
        assertEquals("main", res.getName());
    }

    @Test
    void resourceIsDefinedInSchema() {
        eval("""
                schema vm {
                    var string name
                    var number maxCount = 0
                    var boolean enabled  = true
                }
                resource vm main {
                    name     = "first"
                    maxCount = 1
                    enabled  = false
                }
                """);
        var schema = (SchemaType) checker.getEnv().get("vm");

        assertNotNull(schema);
        assertEquals("vm", schema.getValue());

        var resource = (ResourceType) schema.getInstances().lookup("main");
        assertNotNull(resource);
        assertEquals("main", resource.getName());
        assertEquals(ValueType.String, resource.getProperty("name"));
        assertEquals(ValueType.Number, resource.getProperty("maxCount"));
        assertEquals(ValueType.Boolean, resource.getProperty("enabled"));
    }

    @Test
    void propertyAccessThroughOtherResource() {
        eval("""
                schema vm {
                    var string name
                    var number maxCount = 0
                    var boolean enabled  = true
                }
                resource vm main {
                    name     = "first"
                    maxCount = 1
                    enabled  = false
                }
                resource vm second {
                    name     = "second"
                    maxCount = vm.main.maxCount
                }
                """);
        var schema = (SchemaType) checker.getEnv().get("vm");

        assertNotNull(schema);
        assertEquals("vm", schema.getValue());

        var resource = (ResourceType) schema.getInstances().lookup("main");
        assertNotNull(resource);
        assertEquals("main", resource.getName());
        assertEquals(ValueType.String, resource.getProperty("name"));
        assertEquals(ValueType.Number, resource.getProperty("maxCount"));
        assertEquals(ValueType.Boolean, resource.getProperty("enabled"));

        var second = (ResourceType) schema.getInstances().lookup("second");

        assertNotNull(second);
        assertEquals("second", second.getName());
        assertEquals(ValueType.String, second.getProperty("name"));
        assertEquals(ValueType.Number, second.getProperty("maxCount"));
    }

    @Test
    @DisplayName("throw if a resource uses a field not defined in the schema")
    void resourceThrowsIfFieldNotDefinedInSchema() {
        Assertions.assertThrows(NotFoundException.class, () -> eval("""
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
                   var number x = 2
                }
                
                resource vm main {
                
                }
                """);
        log.warn(res);
        var schema = (SchemaType) checker.getEnv().get("vm");

        var resource = (ResourceType) schema.getInstances().get("main");

        Assertions.assertEquals(ValueType.Number, resource.lookup("x"));
    }

    @Test
    void resourceMemberAccess() {
        var res = eval("""
                schema vm {
                   var number x = 2
                }
                
                resource vm main {
                
                }
                var y = vm.main
                var z = vm.main.x
                z
                """);
        var schema = (SchemaType) checker.getEnv().get("vm");

        var main = (ResourceType) schema.getInstance("main");
        assertSame(ValueType.Number, main.lookup("x"));

        // assert y holds reference to vm.main
        var y = checker.getEnv().lookup("y");
        assertSame(main, y);
        // assert y holds reference to vm.main
        var z = checker.getEnv().lookup("z");
        assertSame(z, schema.getEnvironment().get("x"));

        assertEquals(ValueType.Number, res);
    }

    @Test
    void resourceSetValueOnMissingProperty() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {
                   var number x = 2
                }
                
                resource vm main {
                
                }
                vm.main.y = 3
                """));
    }

    @Test
    void resourceSetValueTypeOnWrongProperty() {
        Assertions.assertThrows(TypeError.class, () -> eval(("""
                schema Vm {
                   var number x = 2
                }
                
                resource Vm main {
                
                }
                Vm.main.x = "test"
                """)));
    }

    @Test
    void resourceInit() {
        eval("""
                schema vm {
                   var string x = "2"
                }
                
                resource vm main {
                    x = "3"
                }
                """);
        var schema = (SchemaType) checker.getEnv().get("vm");
        // default x in schema remains the same
        Assertions.assertEquals(ValueType.String, schema.getProperty("x"));

        var resource = (ResourceType) schema.getInstance("main");
        // x of main resource was updated with a new value
        assertEquals(ValueType.String, resource.getProperty("x"));
    }

    @SneakyThrows
    @Test
    void resourceInitBoolean() {
        eval("""
                schema vm {
                   var boolean x
                }
                
                resource vm main {
                    x = true
                }
                """);
        var schema = (SchemaType) checker.getEnv().get("vm");

        var resource = schema.getInstance("main");

        Assertions.assertInstanceOf(ResourceType.class, resource);
    }

    @Test
    void arrayResourcesAssignedToVar() {
        var array = eval("""
                schema Bucket {
                   var string name
                }
                var envs = [{client: 'amazon'},{client: 'bmw'}]
                [for index in envs]
                resource Bucket photos {
                  name     = 'name-${index.value}'
                }
                """);

        Assertions.assertInstanceOf(ArrayType.class, array);
        var arrayType = (ArrayType) array;
        var resourceType = (ResourceType) arrayType.getType();
        var res = new ResourceType("photos", resourceType.getSchema(), arrayType.getEnvironment());
        assertEquals(res, arrayType.getType());
    }

    @Test
    void testIfConditionReturnsResource() {
        var array = eval("""
                schema Bucket {
                   var string name
                }
                if true {
                    resource Bucket photos {
                      name     = 'name'
                    }
                }
                """);

        Assertions.assertInstanceOf(ResourceType.class, array);
        var resourceType = (ResourceType) array;
        var res = new ResourceType("photos", resourceType.getSchema(), resourceType.getEnvironment());
        assertEquals(res, resourceType);
    }
}
