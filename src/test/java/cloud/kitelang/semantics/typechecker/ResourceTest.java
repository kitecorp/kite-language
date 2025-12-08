package cloud.kitelang.semantics.typechecker;

import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.execution.exceptions.NotFoundException;
import cloud.kitelang.semantics.TypeError;
import cloud.kitelang.semantics.types.ResourceType;
import cloud.kitelang.semantics.types.SchemaType;
import cloud.kitelang.semantics.types.ValueType;
import cloud.kitelang.syntax.ast.ValidationException;
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
        assertThrows(ValidationException.class, () ->
                eval("""
                        schema Server { }
                        resource Server {
                        
                        }
                        """)
        );
    }

    /**
     * This checks for the following syntax
     * main
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
                    string name
                    number maxCount = 0
                    boolean enabled  = true
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

        var resource = (ResourceType) checker.getEnv().get("main");
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
                    string name
                    number maxCount = 0
                    boolean enabled  = true
                }
                resource vm main {
                    name     = "first"
                    maxCount = 1
                    enabled  = false
                }
                resource vm second {
                    name     = "second"
                    maxCount = main.maxCount
                }
                """);
        var schema = (SchemaType) checker.getEnv().get("vm");

        assertNotNull(schema);
        assertEquals("vm", schema.getValue());

        var resource = (ResourceType) checker.getEnv().get("main");
        assertNotNull(resource);
        assertEquals("main", resource.getName());
        assertEquals(ValueType.String, resource.getProperty("name"));
        assertEquals(ValueType.Number, resource.getProperty("maxCount"));
        assertEquals(ValueType.Boolean, resource.getProperty("enabled"));

        var second = (ResourceType) checker.getEnv().get("second");

        assertNotNull(second);
        assertEquals("second", second.getName());
        assertEquals(ValueType.String, second.getProperty("name"));
        assertEquals(ValueType.Number, second.getProperty("maxCount"));
    }

    @Test
    void propertyAccessThroughOtherResourceReverseOrder() {
        eval("""
                schema vm {
                    string name
                    number maxCount = 0
                    boolean enabled = true
                }
                resource vm second {
                    name     = "second"
                    maxCount = main.maxCount
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

        var resource = (ResourceType) checker.getEnv().get("main");
        assertNotNull(resource);
        assertEquals("main", resource.getName());
        assertEquals(ValueType.String, resource.getProperty("name"));
        assertEquals(ValueType.Number, resource.getProperty("maxCount"));
        assertEquals(ValueType.Boolean, resource.getProperty("enabled"));

        var second = (ResourceType) checker.getEnv().get("second");

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
                   number x = 2
                }
                
                resource vm main {
                
                }
                """);
        var schema = (SchemaType) checker.getEnv().get("vm");

        var resource = (ResourceType) checker.getEnv().get("main");

        Assertions.assertEquals(ValueType.Number, resource.lookup("x"));
    }

    @Test
    void resourceMemberAccess() {
        var res = eval("""
                schema vm {
                   number x = 2
                }
                
                resource vm main {
                
                }
                var y = main
                var z = main.x
                z
                """);
        var schema = (SchemaType) checker.getEnv().get("vm");

        var main = (ResourceType) checker.getEnv().get("main");
        assertSame(ValueType.Number, main.lookup("x"));

        // assert y holds reference to main
        var y = checker.getEnv().lookup("y");
        assertSame(main, y);
        // assert y holds reference to main
        var z = checker.getEnv().lookup("z");
        assertSame(z, schema.getEnvironment().get("x"));

        assertEquals(ValueType.Number, res);
    }

    @Test
    void resourceSetValueOnMissingProperty() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {
                   number x = 2
                }
                
                resource vm main {
                
                }
                main.y = 3
                """));
    }

    @Test
    void resourceSetValueTypeOnWrongProperty() {
        Assertions.assertThrows(TypeError.class, () -> eval(("""
                schema Vm {
                   number x = 2
                }
                
                resource Vm main {
                
                }
                main.x = "test"
                """)));
    }

    @Test
    void resourceInit() {
        eval("""
                schema vm {
                   string x = "2"
                }
                
                resource vm main {
                    x = "3"
                }
                """);
        var schema = (SchemaType) checker.getEnv().get("vm");
        // default x in schema remains the same
        Assertions.assertEquals(ValueType.String, schema.getProperty("x"));

        var resource = (ResourceType) checker.getEnv().get("main");
        // x of main resource was updated with a new value
        assertEquals(ValueType.String, resource.getProperty("x"));
    }

    @SneakyThrows
    @Test
    void resourceInitBoolean() {
        eval("""
                schema vm {
                   boolean x
                }
                
                resource vm main {
                    x = true
                }
                """);
        var schema = (SchemaType) checker.getEnv().get("vm");

        var resource = checker.getEnv().get("main");

        Assertions.assertInstanceOf(ResourceType.class, resource);
    }


    @Test
    void testIfConditionReturnsResource() {
        var array = eval("""
                schema Bucket {
                   string name
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
