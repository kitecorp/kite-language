package io.kite.TypeChecker;

import io.kite.Base.CheckerTest;
import io.kite.Runtime.exceptions.InvalidInitException;
import io.kite.TypeChecker.Types.*;
import io.kite.Visitors.PlainTheme;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class ComponentTest extends CheckerTest {

    @Test
    void componentDeclaration() {
        var x = eval("""
                component app {
                
                }
                """);
        assertEquals(new ComponentType("app"), x);
    }

    @Test
    void componentDeclarationDuplicate() {
        checker.getPrinter().setTheme(new PlainTheme());
        var x = Assertions.assertThrows(TypeError.class, () -> eval("""
                component app {
                
                }
                component app {
                
                }
                """));
        Assertions.assertEquals("""
                Component type already exists: component app {
                }
                """, x.getMessage());
    }

    @Test
    void componentDeclarationDuplicateInit() {
        checker.getPrinter().setTheme(new PlainTheme());
        var x = Assertions.assertThrows(InvalidInitException.class, () -> eval("""
                component app {
                
                }
                component app name {
                
                }
                component app name {
                
                }
                """));
        Assertions.assertEquals("""
                Component instance already exists: component app name {
                }
                """, x.getMessage());
    }

    @Test
    void componentInitialization() {
        assertThrows(InvalidInitException.class, () -> eval("""
                component app name {
                
                }
                """));
    }

    @Test
    void componentDeclarationAndInitialization() {
        var x = eval("""
                component app {
                
                }
                component app first {
                
                }
                """);
        assertEquals(new ComponentType("app", "first", null), x);
    }

    @Test
    void componentDeclarationWithResource() {
        var res = eval("""
                schema vm {
                    string name
                }
                
                component app {
                    resource vm main {
                        name     = "first"
                    }
                }
                """);
        var resourceType = new ResourceType("main", new SchemaType("vm"), new TypeEnvironment(checker.getEnv()));
        resourceType.getEnvironment().init("name", ValueType.String);
        var componentType = new ComponentType("app", new TypeEnvironment(checker.getEnv(), Map.of("main", resourceType)));
        assertEquals(componentType, res);

        assertEquals(componentType.getEnvironment(), ((ComponentType) res).getEnvironment());
        var main = (ResourceType) componentType.getEnvironment().lookup("main");
        assertEquals(ValueType.String, main.getProperty("name"));
    }

    @Test
    void componentDeclarationWithNestedComponentDefinition() {
        var res = eval("""
            schema vm {
                string name
            }
            
            component app {
                component database {
                    resource vm db_server {
                        name = "database"
                    }
                }
                
                resource vm web_server {
                    name = "webserver"
                }
            }
            """);

        // Verify res is a ComponentType
        assertInstanceOf(ComponentType.class, res);
        var appComponentType = (ComponentType) res;
        assertEquals("app", appComponentType.getType());

        // Verify the nested component definition exists in app's environment
        var nestedDatabase = appComponentType.getEnvironment().lookup("database");
        assertNotNull(nestedDatabase, "Nested database component should exist");
        assertInstanceOf(ComponentType.class, nestedDatabase);

        var databaseComponent = (ComponentType) nestedDatabase;
        assertEquals("database", databaseComponent.getType());

        // Verify nested component has its resource
        var dbServer = databaseComponent.getEnvironment().lookup("db_server");
        assertNotNull(dbServer, "db_server resource should exist in database component");
        assertInstanceOf(ResourceType.class, dbServer);

        var dbServerResource = (ResourceType) dbServer;
        assertEquals("db_server", dbServerResource.getName());
        assertEquals("vm", dbServerResource.getSchema().name());
        assertEquals(ValueType.String, dbServerResource.getProperty("name"));

        // Verify outer component has its resource
        var webServer = appComponentType.getEnvironment().lookup("web_server");
        assertNotNull(webServer, "web_server resource should exist in app component");
        assertInstanceOf(ResourceType.class, webServer);

        var webServerResource = (ResourceType) webServer;
        assertEquals("web_server", webServerResource.getName());
        assertEquals("vm", webServerResource.getSchema().name());
        assertEquals(ValueType.String, webServerResource.getProperty("name"));
    }

    @Test
    void componentDeclarationWithResourceThrows() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {
                    number name
                }
                
                component app {
                    resource vm main {
                        name     = "first"
                    }
                }
                """));
    }

//    @Test
//    void newResourceThrowsIfNoNameIsSpecified() {
//        eval("""
//                schema Server { }
//                resource Server {
//
//                }
//                """);
//        Assertions.assertFalse(ParserErrors.getErrors().isEmpty());
//    }
//
//    /**
//     * This checks for the following syntax
//     * vm.main
//     * All resources are defined in the schema
//     * global env{
//     * vm   SchemaValue -> variables{ main -> resource vm}
//     * }
//     */
//    @Test
//    void resourceIsDefinedInSchemaEnv() {
//        ResourceType res = (ResourceType) eval("""
//                schema Server { }
//                resource Server main  {
//
//                }
//                """);
//        var schema = (SchemaType) checker.getEnv().get("Server");
//
//        assertNotNull(schema);
//        assertEquals(SchemaType.class, schema.getClass());
//        assertEquals("Server", schema.getValue());
//
//        assertNotNull(res);
//        assertEquals("main", res.getName());
//    }
//
//    @Test
//    void resourceIsDefinedInSchema() {
//        eval("""
//                schema vm {
//                    string name
//                    number maxCount = 0
//                    boolean enabled  = true
//                }
//                resource vm main {
//                    name     = "first"
//                    maxCount = 1
//                    enabled  = false
//                }
//                """);
//        var schema = (SchemaType) checker.getEnv().get("vm");
//
//        assertNotNull(schema);
//        assertEquals("vm", schema.getValue());
//
//        var resource = (ResourceType) schema.getInstances().lookup("main");
//        assertNotNull(resource);
//        assertEquals("main", resource.getName());
//        assertEquals(ValueType.String, resource.getProperty("name"));
//        assertEquals(ValueType.Number, resource.getProperty("maxCount"));
//        assertEquals(ValueType.Boolean, resource.getProperty("enabled"));
//    }
//
//    @Test
//    void propertyAccessThroughOtherResource() {
//        eval("""
//                schema vm {
//                    string name
//                    number maxCount = 0
//                    boolean enabled  = true
//                }
//                resource vm main {
//                    name     = "first"
//                    maxCount = 1
//                    enabled  = false
//                }
//                resource vm second {
//                    name     = "second"
//                    maxCount = vm.main.maxCount
//                }
//                """);
//        var schema = (SchemaType) checker.getEnv().get("vm");
//
//        assertNotNull(schema);
//        assertEquals("vm", schema.getValue());
//
//        var resource = (ResourceType) schema.getInstances().lookup("main");
//        assertNotNull(resource);
//        assertEquals("main", resource.getName());
//        assertEquals(ValueType.String, resource.getProperty("name"));
//        assertEquals(ValueType.Number, resource.getProperty("maxCount"));
//        assertEquals(ValueType.Boolean, resource.getProperty("enabled"));
//
//        var second = (ResourceType) schema.getInstances().lookup("second");
//
//        assertNotNull(second);
//        assertEquals("second", second.getName());
//        assertEquals(ValueType.String, second.getProperty("name"));
//        assertEquals(ValueType.Number, second.getProperty("maxCount"));
//    }
//
//    @Test
//    void propertyAccessThroughOtherResourceReverseOrder() {
//        eval("""
//                schema vm {
//                    string name
//                    number maxCount = 0
//                    boolean enabled = true
//                }
//                resource vm second {
//                    name     = "second"
//                    maxCount = vm.main.maxCount
//                }
//
//                resource vm main {
//                    name     = "first"
//                    maxCount = 1
//                    enabled  = false
//                }
//                """);
//        var schema = (SchemaType) checker.getEnv().get("vm");
//
//        assertNotNull(schema);
//        assertEquals("vm", schema.getValue());
//
//        var resource = (ResourceType) schema.getInstances().lookup("main");
//        assertNotNull(resource);
//        assertEquals("main", resource.getName());
//        assertEquals(ValueType.String, resource.getProperty("name"));
//        assertEquals(ValueType.Number, resource.getProperty("maxCount"));
//        assertEquals(ValueType.Boolean, resource.getProperty("enabled"));
//
//        var second = (ResourceType) schema.getInstances().lookup("second");
//
//        assertNotNull(second);
//        assertEquals("second", second.getName());
//        assertEquals(ValueType.String, second.getProperty("name"));
//        assertEquals(ValueType.Number, second.getProperty("maxCount"));
//    }
//
//    @Test
//    @DisplayName("throw if a resource uses a field not defined in the schema")
//    void resourceThrowsIfFieldNotDefinedInSchema() {
//        Assertions.assertThrows(NotFoundException.class, () -> eval("""
//                schema vm {
//                }
//
//                resource vm main {
//                    x = 3
//                }
//                """));
//    }
//
//
//    @Test
//    void resourceInheritsDefaultSchemaValue() {
//        var res = eval("""
//                schema vm {
//                   number x = 2
//                }
//
//                resource vm main {
//
//                }
//                """);
//        log.warn(res);
//        var schema = (SchemaType) checker.getEnv().get("vm");
//
//        var resource = (ResourceType) schema.getInstances().get("main");
//
//        Assertions.assertEquals(ValueType.Number, resource.lookup("x"));
//    }
//
//    @Test
//    void resourceMemberAccess() {
//        var res = eval("""
//                schema vm {
//                   number x = 2
//                }
//
//                resource vm main {
//
//                }
//                var y = vm.main
//                var z = vm.main.x
//                z
//                """);
//        var schema = (SchemaType) checker.getEnv().get("vm");
//
//        var main = (ResourceType) schema.getInstance("main");
//        assertSame(ValueType.Number, main.lookup("x"));
//
//        // assert y holds reference to vm.main
//        var y = checker.getEnv().lookup("y");
//        assertSame(main, y);
//        // assert y holds reference to vm.main
//        var z = checker.getEnv().lookup("z");
//        assertSame(z, schema.getEnvironment().get("x"));
//
//        assertEquals(ValueType.Number, res);
//    }
//
//    @Test
//    void resourceSetValueOnMissingProperty() {
//        Assertions.assertThrows(TypeError.class, () -> eval("""
//                schema vm {
//                   number x = 2
//                }
//
//                resource vm main {
//
//                }
//                vm.main.y = 3
//                """));
//    }
//
//    @Test
//    void resourceSetValueTypeOnWrongProperty() {
//        Assertions.assertThrows(TypeError.class, () -> eval(("""
//                schema Vm {
//                   number x = 2
//                }
//
//                resource Vm main {
//
//                }
//                Vm.main.x = "test"
//                """)));
//    }
//
//    @Test
//    void resourceInit() {
//        eval("""
//                schema vm {
//                   string x = "2"
//                }
//
//                resource vm main {
//                    x = "3"
//                }
//                """);
//        var schema = (SchemaType) checker.getEnv().get("vm");
//        // default x in schema remains the same
//        Assertions.assertEquals(ValueType.String, schema.getProperty("x"));
//
//        var resource = (ResourceType) schema.getInstance("main");
//        // x of main resource was updated with a new value
//        assertEquals(ValueType.String, resource.getProperty("x"));
//    }
//
//    @SneakyThrows
//    @Test
//    void resourceInitBoolean() {
//        eval("""
//                schema vm {
//                   boolean x
//                }
//
//                resource vm main {
//                    x = true
//                }
//                """);
//        var schema = (SchemaType) checker.getEnv().get("vm");
//
//        var resource = schema.getInstance("main");
//
//        Assertions.assertInstanceOf(ResourceType.class, resource);
//    }
//
//
//    @Test
//    void testIfConditionReturnsResource() {
//        var array = eval("""
//                schema Bucket {
//                   string name
//                }
//                if true {
//                    resource Bucket photos {
//                      name     = 'name'
//                    }
//                }
//                """);
//
//        Assertions.assertInstanceOf(ResourceType.class, array);
//        var resourceType = (ResourceType) array;
//        var res = new ResourceType("photos", resourceType.getSchema(), resourceType.getEnvironment());
//        assertEquals(res, resourceType);
//    }
}
