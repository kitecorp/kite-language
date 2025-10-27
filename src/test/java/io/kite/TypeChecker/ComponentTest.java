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

    // Helper methods for component testing
    private static ComponentType assertIsComponentType(Type type, String expectedTypeName) {
        assertInstanceOf(ComponentType.class, type, "Expected a ComponentType");
        var component = (ComponentType) type;
        assertEquals(expectedTypeName, component.getType(), "Component type name mismatch");
        return component;
    }

    private static ResourceType assertIsResourceType(Type type, String expectedName, String expectedSchemaType) {
        assertInstanceOf(ResourceType.class, type, "Expected a ResourceType");
        var resource = (ResourceType) type;
        assertEquals(expectedName, resource.getName(), "Resource name mismatch");

        if (expectedSchemaType != null) {
            assertInstanceOf(SchemaType.class, resource.getSchema(), "Expected SchemaType for resource");
            var schema = resource.getSchema();
            assertEquals(expectedSchemaType, schema.name(), "Schema type mismatch");
        }

        return resource;
    }

    private static Type assertHasInEnvironment(ComponentType component, String key, String errorMessage) {
        var value = component.lookup(key); // Use helper instead of getEnvironment().lookup()
        assertNotNull(value, errorMessage != null ? errorMessage : key + " should exist in environment");
        return value;
    }

    private static ResourceType assertComponentHasResource(ComponentType component, String resourceName, String schemaType) {
        var resourceType = assertHasInEnvironment(component, resourceName, null);
        return assertIsResourceType(resourceType, resourceName, schemaType); // Return the ResourceType
    }

    private static ComponentType assertComponentHasNestedComponent(ComponentType parent, String nestedName) {
        var nestedType = assertHasInEnvironment(parent, nestedName, null);
        return assertIsComponentType(nestedType, nestedName); // Return the ComponentType
    }

    private static void assertResourceProperty(ResourceType resource, String propertyName, Type expectedType) {
        var propertyValue = resource.getProperty(propertyName);
        assertEquals(expectedType, propertyValue,
                "Property " + propertyName + " should be " + expectedType);
    }

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
        var componentType = new ComponentType("app", new TypeEnvironment("app", checker.getEnv(), Map.of("main", resourceType)));
        assertEquals(componentType, res);

        assertEquals(componentType.getEnvironment(), ((ComponentType) res).getEnvironment());
        var main = (ResourceType) componentType.getEnvironment().lookup("main");
        assertEquals(ValueType.String, main.getProperty("name"));
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
                """), "throws because name is a number (in schema) and we assign a string");
    }

    @Test
    void componentDeclarationWithNestedComponentDefinition() {
        var res = (Type) eval("""
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

        // Verify app component structure
        var appComponent = assertIsComponentType(res, "app");

        // Get nested components and resources without casting
        var databaseComponent = assertComponentHasNestedComponent(appComponent, "database");
        var webServer = assertComponentHasResource(appComponent, "web_server", "vm");

        // Verify nested database component structure
        var dbServer = assertComponentHasResource(databaseComponent, "db_server", "vm");

        // Verify properties
        assertResourceProperty(dbServer, "name", ValueType.String);
        assertResourceProperty(webServer, "name", ValueType.String);
    }

    @Test
    void componentInitializationInsideComponentShouldFail() {
        checker.getPrinter().setTheme(new PlainTheme());
        var exception = assertThrows(InvalidInitException.class, () -> eval("""
                schema vm {
                    string name
                }
                
                component database {
                    resource vm db_server {
                        name = "database"
                    }
                }
                
                component app {
                    // This should fail - initialization inside a component
                    component database prod_db {
                
                    }
                }
                """));

        assertEquals("Component initialization not allowed inside component definition: component database prod_db {\n}\n",
                exception.getMessage());
    }

    @Test
    void componentDeclarationWithInputAndDefaultValue() {
        var res = (Type) eval("""
            schema vm {
                string name
            }
            
            component database {
                input string dbName = "default"
                
                resource vm db_server {
                    name = dbName
                }
            }
            """);

        // Verify component type
        var databaseComponent = assertIsComponentType(res, "database");

        // Verify input exists
        var dbName = databaseComponent.lookup("dbName");
        assertNotNull(dbName, "Input 'dbName' should exist");
        assertEquals(ValueType.String, dbName, "Input should be of type string");

        // Verify resource exists
        var dbServer = assertComponentHasResource(databaseComponent, "db_server", "vm");

        // Verify resource property 'name' is of type string
        assertResourceProperty(dbServer, "name", ValueType.String);
    }

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
