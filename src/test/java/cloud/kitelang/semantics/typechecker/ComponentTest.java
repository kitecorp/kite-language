package cloud.kitelang.semantics;

import cloud.kitelang.base.CheckerTest;
import cloud.kitelang.execution.exceptions.DeclarationExistsException;
import cloud.kitelang.execution.exceptions.InvalidInitException;
import cloud.kitelang.execution.exceptions.NotFoundException;
import cloud.kitelang.semantics.types.*;
import cloud.kitelang.tool.theme.PlainTheme;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static cloud.kitelang.semantics.types.ArrayType.arrayType;
import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class ComponentTest extends CheckerTest {

    // Helper methods for component testing
    public static ComponentType assertIsComponentType(Type type, String expectedTypeName) {
        assertInstanceOf(ComponentType.class, type, "Expected a ComponentType");
        var component = (ComponentType) type;
        assertEquals(expectedTypeName, component.getType(), "Component type name mismatch");
        return component;
    }

    public static ResourceType assertIsResourceType(Type type, String expectedName, String expectedSchemaType) {
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

    public static Type assertHasInEnvironment(ComponentType component, String key, String errorMessage) {
        var value = component.lookup(key); // Use helper instead of getEnvironment().lookup()
        assertNotNull(value, errorMessage != null ? errorMessage : key + " should exist in environment");
        return value;
    }

    public static ResourceType assertComponentHasResource(ComponentType component, String resourceName, String schemaType) {
        var resourceType = assertHasInEnvironment(component, resourceName, null);
        return assertIsResourceType(resourceType, resourceName, schemaType); // Return the ResourceType
    }

    public static ComponentType assertComponentHasNestedComponent(ComponentType parent, String nestedName) {
        var nestedType = assertHasInEnvironment(parent, nestedName, null);
        return assertIsComponentType(nestedType, nestedName); // Return the ComponentType
    }

    public static void assertResourceProperty(ResourceType resource, String propertyName, Type expectedType) {
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
        var res = eval("""
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

    @Test
    void componentInitializationWithInputValue() {
        var res = eval("""
                schema vm {
                    string name
                }
                
                component database {
                    input string dbName
                
                    resource vm db_server {
                        name = dbName
                    }
                }
                
                component database prod_db {
                    dbName = "production"
                }
                """);

        // Verify instance was created
        var prodDbInstance = assertIsComponentType(res, "database");
        assertEquals("prod_db", prodDbInstance.getName());

        // Verify input has the provided value
        var dbName = prodDbInstance.lookup("dbName");
        assertNotNull(dbName, "Input 'dbName' should exist");
        assertEquals(ValueType.String, dbName, "Input should be of type string");

        // Verify resource exists
        var dbServer = assertComponentHasResource(prodDbInstance, "db_server", "vm");

        // Verify resource property 'name' is of type string
        assertResourceProperty(dbServer, "name", ValueType.String);
    }

    @Test
    void componentInitializationWithWrongInputType() {
        checker.getPrinter().setTheme(new PlainTheme());
        var exception = assertThrows(TypeError.class, () -> eval("""
                schema vm {
                    string name
                }
                
                component database {
                    input string dbName
                
                    resource vm db_server {
                        name = dbName
                    }
                }
                
                component database prod_db {
                    dbName = 123
                }
                """));

        assertEquals("Expected type `string` but got `number` in expression: dbName = 123",
                exception.getMessage());
    }

    @Test
    void componentInitializationWithMissingInputType() {
        checker.getPrinter().setTheme(new PlainTheme());
        var exception = assertThrows(NotFoundException.class, () -> eval("""
                schema vm {
                    string name
                }
                
                component database {
                    input string dbName
                
                    resource vm db_server {
                        name = dbName
                    }
                }
                
                component database prod_db {
                    dbNameee = 123 
                }
                """));

        assertEquals("Variable not found: dbNameee", exception.getMessage());
    }

    @Test
    void componentWithResourceReferencingAnotherResource() {
        var res = eval("""
                schema vm {
                    string name
                }
                
                schema network {
                    string cidr
                }
                
                component app {
                    resource network vpc {
                        cidr = "10.0.0.0/16"
                    }
                
                    resource vm web_server {
                        name = vpc.cidr
                    }
                }
                """);

        // Verify component type
        var appComponent = assertIsComponentType(res, "app");

        // Verify both resources exist
        var vpcResource = assertComponentHasResource(appComponent, "vpc", "network");
        var webServerResource = assertComponentHasResource(appComponent, "web_server", "vm");

        // Verify vpc resource has cidr property
        assertResourceProperty(vpcResource, "cidr", ValueType.String);

        // Verify web_server references vpc's property (type should be string)
        assertResourceProperty(webServerResource, "name", ValueType.String);
    }

    @Test
    void resourceReferenceToNonExistentProperty() {
        checker.getPrinter().setTheme(new PlainTheme());
        var exception = assertThrows(TypeError.class, () -> eval("""
                schema network {
                    string id
                    string cidr
                }
                
                schema vm {
                    string name
                }
                
                component app {
                    resource network vpc {
                        id = "vpc-123"
                        cidr = "10.0.0.0/16"
                    }
                
                    resource vm web_server {
                        name = vpc.nonexistent
                    }
                }
                """));

        assertTrue(exception.getMessage().contains("nonexistent")
                   || exception.getMessage().contains("vpc"),
                "Error should indicate the property doesn't exist");
    }

    @Test
    void resourceReferenceWithTypeMismatch() {
        checker.getPrinter().setTheme(new PlainTheme());
        var exception = assertThrows(TypeError.class, () -> eval("""
                schema network {
                    string cidr
                }
                
                schema vm {
                    string name
                    number cpu_count
                }
                
                component app {
                    resource network vpc {
                        cidr = "10.0.0.0/16"
                    }
                
                    resource vm web_server {
                        name = "web"
                        cpu_count = vpc.cidr
                    }
                }
                """));

        assertTrue(exception.getMessage().contains("number")
                   && exception.getMessage().contains("string"),
                "Error should indicate type mismatch between number and string");
    }

    @Test
    void crossComponentResourceReference() {
        var res = eval("""
                schema vm {
                    string id
                    string networkId
                }
                
                component networking {
                    resource vm vpc {
                        id = "vpc-123"
                    }
                
                    output string vpcId = vpc.id
                }
                
                component app {
                    resource vm webServer {
                        networkId = networking.vpcId
                    }
                }
                """);

        // Verify both components exist
        var appComponent = assertIsComponentType(res, "app");

        // Look up networking component from the environment
        var networkingComponent = (ComponentType) checker.getEnv().lookup("networking");
        assertNotNull(networkingComponent, "Networking component should exist");

        // Verify resources
        var vpcResource = assertComponentHasResource(networkingComponent, "vpc", "vm");
        assertResourceProperty(vpcResource, "id", ValueType.String);

        // Verify output
        var vpcOutput = networkingComponent.lookup("vpcId");
        assertEquals(ValueType.String, vpcOutput);

        var webServerResource = assertComponentHasResource(appComponent, "webServer", "vm");
        assertResourceProperty(webServerResource, "networkId", ValueType.String);
    }

    @Test
    void componentDefinitionCannotAccessAnotherDefinitionResource() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {
                    string id
                    string networkId
                }
                
                component networking {
                    resource vm vpc {
                        id = "vpc-123"
                    }
                }
                
                component app {
                    resource vm webServer {
                        networkId = networking.vpc.id
                    }
                }
                """));
    }

    @Test
    void crossComponentInstanceResourceReference() {
        var res = eval("""
                schema vm {
                    string id
                    string networkId
                }
                
                component networking {
                    input string vpcId
                
                    resource vm vpc {
                        id = vpcId
                    }
                
                    output string vpcOutputId = vpc.id
                }
                
                component app {
                    input string netRef
                
                    resource vm webServer {
                        networkId = netRef
                    }
                }
                
                component networking prodNet {
                    vpcId = "vpc-production"
                }
                
                component app prodApp {
                    netRef = prodNet.vpcOutputId
                }
                """);

        // Verify the prodApp instance was created
        var prodAppInstance = assertIsComponentType(res, "app");
        assertEquals("prodApp", prodAppInstance.getName());

        // Verify prodNet instance exists
        var prodNetInstance = (ComponentType) checker.getEnv().lookup("prodNet");
        assertNotNull(prodNetInstance, "prodNet instance should exist");
        assertEquals("prodNet", prodNetInstance.getName());

        // Verify resources exist and types are correct
        var vpcResource = assertComponentHasResource(prodNetInstance, "vpc", "vm");
        assertResourceProperty(vpcResource, "id", ValueType.String);

        // Verify output exists
        var vpcOutput = prodNetInstance.lookup("vpcOutputId");
        assertEquals(ValueType.String, vpcOutput);

        var webServerResource = assertComponentHasResource(prodAppInstance, "webServer", "vm");
        assertResourceProperty(webServerResource, "networkId", ValueType.String);
    }

    @Test
    void componentInstanceCannotAccessComponentDefinition() {
        checker.getPrinter().setTheme(new PlainTheme());
        var exception = assertThrows(TypeError.class, () -> eval("""
                schema vm {
                    string id
                    string networkId
                }
                
                component networking {
                    input string vpcId
                
                    resource vm vpc {
                        id = vpcId
                    }
                }
                
                component app {
                    input string netRef
                
                    resource vm webServer {
                        networkId = netRef
                    }
                }
                
                component app prodApp {
                    netRef = networking.vpc.id
                }
                """));

        assertEquals("Cannot access component definition 'networking.vpc'. Only component instances can be referenced.", exception.getMessage(),
                "Error should indicate cannot access component definition from instance");
    }

    @Test
    void outputInComponent() {
        var res = eval("""
                schema vm {
                    string id
                }
                
                component app {
                    resource vm server {
                        id = "server-123"
                    }
                
                    output string serverId = server.id
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        var outputType = appComponent.lookup("serverId");
        assertNotNull(outputType, "Output should exist in component");
        assertEquals(ValueType.String, outputType);
    }

    @Test
    void outputInComponentFromInput() {
        var res = eval("""
                schema vm {
                    string id
                }
                
                component app {
                    input string inputId
                
                    resource vm server {
                        id = inputId
                    }
                
                    output string serverId = server.id
                }
                """);

        var appComponent = assertIsComponentType(res, "app");

        // Verify input exists
        var inputType = appComponent.lookup("inputId");
        assertEquals(ValueType.String, inputType);

        // Verify output exists
        var outputType = appComponent.lookup("serverId");
        assertEquals(ValueType.String, outputType);
    }

    @Test
    void outputInComponentInstance() {
        var res = eval("""
                schema vm {
                    string id
                }
                
                component app {
                    input string inputId
                
                    resource vm server {
                        id = inputId
                    }
                
                    output string serverId = server.id
                }
                
                component app prodApp {
                    inputId = "prod-123"
                }
                """);

        var prodAppInstance = assertIsComponentType(res, "app");
        assertEquals("prodApp", prodAppInstance.getName());

        // Verify output exists in instance
        var outputType = prodAppInstance.lookup("serverId");
        assertNotNull(outputType, "Output should exist in component instance");
        assertEquals(ValueType.String, outputType);
    }

    @Test
    void outputTypeMismatchInComponent() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {
                    string id
                }
                
                component app {
                    resource vm server {
                        id = "server-123"
                    }
                
                    output number serverId = server.id
                }
                """));
    }

    @Test
    void outputDuplicateInComponent() {
        assertThrows(DeclarationExistsException.class, () -> eval("""
                schema vm {
                    string id
                }
                
                component app {
                    resource vm server {
                        id = "server-123"
                    }
                
                    output string serverId = server.id
                    output string serverId = server.id
                }
                """));
    }

    @Test
    void outputAccessFromAnotherComponentInstance() {
        var res = eval("""
                schema vm {
                    string id
                    string networkId
                }
                
                component networking {
                    input string vpcId
                
                    resource vm vpc {
                        id = vpcId
                    }
                
                    output string vpcOutputId = vpc.id
                }
                
                component app {
                    input string netRef
                
                    resource vm webServer {
                        networkId = netRef
                    }
                }
                
                component networking prodNet {
                    vpcId = "vpc-production"
                }
                
                component app prodApp {
                    netRef = prodNet.vpcOutputId
                }
                """);

        // Verify prodApp instance was created
        var prodAppInstance = assertIsComponentType(res, "app");
        assertEquals("prodApp", prodAppInstance.getName());

        // Verify prodNet instance exists with output
        var prodNetInstance = (ComponentType) checker.getEnv().lookup("prodNet");
        assertNotNull(prodNetInstance);
        var outputType = prodNetInstance.lookup("vpcOutputId");
        assertEquals(ValueType.String, outputType);
    }

    @Test
    void outputReferencingNonExistentResource() {
        assertThrows(NotFoundException.class, () -> eval("""
                component app {
                    output string serverId = server.id
                }
                """));
    }

    @Test
    void outputReferencingNonExistentProperty() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {
                    string id
                }
                
                component app {
                    resource vm server {
                        id = "server-123"
                    }
                
                    output string serverId = server.nonExistent
                }
                """));
    }

    @Test
    void outputReferencingAnotherOutput() {
        var res = eval("""
                schema vm {
                    string id
                }
                
                component app {
                    resource vm server {
                        id = "server-123"
                    }
                
                    output string serverId = server.id
                    output string serverIdCopy = serverId
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        assertEquals(ValueType.String, appComponent.lookup("serverId"));
        assertEquals(ValueType.String, appComponent.lookup("serverIdCopy"));
    }

    @Test
    void outputReferencingInput() {
        var res = eval("""
                component app {
                    input string name = "test"
                    output string outputName = name
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        assertEquals(ValueType.String, appComponent.lookup("outputName"));
    }

    @Test
    void outputInNestedComponent() {
        var res = eval("""
                schema vm {
                    string id
                }
                
                component app {
                    component database {
                        resource vm db {
                            id = "db-123"
                        }
                
                        output string dbId = db.id
                    }
                
                    resource vm web {
                        id = "web-123"
                    }
                
                    output string webId = web.id
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        var databaseComponent = (ComponentType) appComponent.lookup("database");

        // Both components should have their outputs
        assertEquals(ValueType.String, databaseComponent.lookup("dbId"));
        assertEquals(ValueType.String, appComponent.lookup("webId"));
    }

    @Test
    void outputAccessingNestedComponentOutput() {
        var res = eval("""
                schema vm {
                    string id
                }
                
                component app {
                    component database {
                        resource vm db {
                            id = "db-123"
                        }
                
                        output string dbId = db.id
                    }
                
                    resource vm web {
                        id = database.dbId
                    }
                
                    output string webId = web.id
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        assertEquals(ValueType.String, appComponent.lookup("webId"));
    }

    @Test
    void outputInComponentInstanceCannotAddNewOutputs() {
        // Component instances should not be able to declare new outputs
        assertThrows(TypeError.class, () -> eval("""
                schema vm {
                    string id
                }
                
                component app {
                    input string inputId
                    resource vm server {
                        id = inputId
                    }
                }
                
                component app prodApp {
                    inputId = "prod-123"
                    output string newOutput = "test"
                }
                """));
    }


    @Test
    void outputArrayType() {
        var res = eval("""
                schema vm {
                    string id
                }
                
                component app {
                    resource vm server1 {
                        id = "s1"
                    }
                    resource vm server2 {
                        id = "s2"
                    }
                
                    output string[] serverIds = [server1.id, server2.id]
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        assertEquals(arrayType(ValueType.String), appComponent.lookup("serverIds"));
    }

    @Test
    void outputObjectType() {
        var res = eval("""
                schema vm {
                    string id
                }
                
                component app {
                    resource vm server {
                        id = "server-123"
                    }
                
                    output object serverInfo = {
                        id: server.id,
                        name: "prod"
                    }
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        assertEquals(ObjectType.INSTANCE, appComponent.lookup("serverInfo"));
    }

    @Test
    void multipleOutputsInComponent() {
        var res = eval("""
                schema vm {
                    string id
                    string name
                }
                
                component app {
                    resource vm server {
                        id = "server-123"
                        name = "prod-server"
                    }
                
                    output string serverId = server.id
                    output string serverName = server.name
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        assertEquals(ValueType.String, appComponent.lookup("serverId"));
        assertEquals(ValueType.String, appComponent.lookup("serverName"));
    }

    @Test
    void outputReferencingMultipleResources() {
        var res = eval("""
                schema vm {
                    string id
                }
                
                component app {
                    resource vm server1 {
                        id = "s1"
                    }
                    resource vm server2 {
                        id = "s2"
                    }
                
                    output string combinedId = server1.id + server2.id
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        assertEquals(ValueType.String, appComponent.lookup("combinedId"));
    }

    @Test
    void outputWithComputedExpression() {
        var res = eval("""
                component app {
                    input number count = 5
                    output number doubled = count * 2
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        assertEquals(ValueType.Number, appComponent.lookup("doubled"));
    }


    @Test
    void crossComponentChainedOutputs() {
        var res = eval("""
                schema vm {
                    string id
                    string refId
                }
                
                component base {
                    resource vm vpc {
                        id = "vpc-1"
                    }
                    output string vpcId = vpc.id
                }
                
                component middle {
                    input string baseRef
                    resource vm subnet {
                        id = "subnet-1"
                        refId = baseRef
                    }
                    output string subnetId = subnet.id
                }
                
                component app {
                    input string middleRef
                    resource vm instance {
                        id = "instance-1"
                        refId = middleRef
                    }
                }
                
                component base baseInst {
                }
                
                component middle middleInst {
                    baseRef = baseInst.vpcId
                }
                
                component app appInst {
                    middleRef = middleInst.subnetId
                }
                """);

        // Verify the chain of outputs works
        var appInstance = assertIsComponentType(res, "app");
        assertEquals("appInst", appInstance.getName());
    }

    @Test
    void outputBeforeResourceDeclaration() {
        var res = eval("""
                schema vm {
                    string id
                }

                component app {
                    output string serverId = server.id

                    resource vm server {
                        id = "server-123"
                    }
                }
                """);
        // Verify the component was created and forward reference resolved
        var appComponent = assertIsComponentType(res, "app");
        var outputType = appComponent.lookup("serverId");
        assertEquals(ValueType.String, outputType);
    }

    @Test
    void componentInstanceCannotAccessAnotherInstanceResource() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {
                    string id
                    string networkId
                }
                
                component networking {
                    input string vpcId
                
                    resource vm vpc {
                        id = vpcId
                    }
                
                    output string vpcOutputId = vpc.id
                }
                
                component app {
                    input string netRef
                
                    resource vm webServer {
                        networkId = netRef
                    }
                }
                
                component networking prodNet {
                    vpcId = "vpc-production"
                }
                
                component app prodApp {
                    netRef = prodNet.vpc.id
                }
                """));
    }

    @Test
    @Disabled
    void componentInputsNotAccessibleFromOutside() {
        assertThrows(TypeError.class, () -> eval("""
                component app {
                    input string name = "test"
                }
                
                component other {
                    input string ref = app.name
                }
                """));
    }

    @Test
    void componentWithOnlyOutputs() {
        var res = eval("""
                component config {
                    input string env = "prod"
                    output string environment = env
                    output string region = "us-east-1"
                }
                """);

        var configComponent = assertIsComponentType(res, "config");
        assertEquals(ValueType.String, configComponent.lookup("environment"));
        assertEquals(ValueType.String, configComponent.lookup("region"));
    }

    @Test
    void nestedComponentsCanAccessSiblingOutputs() {
        var res = eval("""
                schema vm {
                    string id
                    string dbRef
                }
                
                component app {
                    component database {
                        resource vm db {
                            id = "db-123"
                        }
                        output string dbId = db.id
                    }
                
                    component api {
                        resource vm server {
                            id = "api-123"
                            dbRef = database.dbId
                        }
                    }
                }
                """);

        // Should work - sibling components can reference each other's outputs
    }

    @Test
    void parentCanAccessNestedComponentOutput() {
        var res = eval("""
                schema vm {
                    string id
                }
                
                component app {
                    component database {
                        resource vm db {
                            id = "db-123"
                        }
                        output string dbId = db.id
                    }
                
                    output string exposedDbId = database.dbId
                }
                """);

        var appComponent = assertIsComponentType(res, "app");
        assertEquals(ValueType.String, appComponent.lookup("exposedDbId"));
    }

}
