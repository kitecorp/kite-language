package io.kite.runtime;

import io.kite.base.RuntimeTest;
import io.kite.environment.Environment;
import io.kite.runtime.exceptions.DeclarationExistsException;
import io.kite.runtime.values.ResourceValue;
import io.kite.runtime.values.SchemaValue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.kite.runtime.values.ResourceValue.resourceValue;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("For loop resources")
public class ForResourceTest extends RuntimeTest {

    /**
     * Helper method to create expected ResourceValue for test assertions.
     * Simplifies verbose ResourceValue creation in tests.
     *
     * @param resourceName The resource name (e.g., "main")
     * @param envName      The environment name (e.g., "main")
     * @param properties   The properties map
     * @param schemaType   The schema type (e.g., "vm", "Bucket")
     * @param path         The resource path (e.g., "vm.main[\"prod\"]")
     * @return A ResourceValue for comparison
     */
    private ResourceValue expected(String resourceName, String envName, Map<String, Object> properties, String schemaType, String path) {
        return resourceValue(resourceName,
                Environment.of(envName, properties),
                interpreter.getSchema(schemaType),
                ResourcePath.parse(path));
    }

    @Test
    @DisplayName("Resolve var name using inside a loop")
    void testForReturnsResourceNestedVar() {
        var res = eval("""
                schema vm {
                   string name
                }
                for i in 0..2 {
                    var name = 'prod'
                    resource vm main {
                      name     = name
                    }
                }
                """);

        var resource0 = interpreter.getInstance("main[0]");
        var resource1 = interpreter.getInstance("main[1]");

        assertInstanceOf(ResourceValue.class, resource0);
        assertInstanceOf(ResourceValue.class, resource1);
        assertEquals(resource1, res);
    }

    @Test
    @DisplayName("Test multiple resources")
    void multipleResources() {
        var res = eval("""
                schema vm {
                   string name
                }
                for i in 0..2 {
                    var name = 'prod'
                    resource vm main {
                      name     = name
                    }
                    resource vm second {
                      name     = name
                    }
                }
                """);

        var schema = interpreter.getSchema("vm");

        var resource0 = interpreter.getInstance("main[0]");
        var resource1 = interpreter.getInstance("main[1]");
        assertInstanceOf(ResourceValue.class, resource0);
        assertInstanceOf(ResourceValue.class, resource1);

        var second0 = interpreter.getInstance("second[0]");
        var second1 = interpreter.getInstance("second[1]");
        assertInstanceOf(ResourceValue.class, second0);
        assertInstanceOf(ResourceValue.class, second1);
    }

    @Test
    void rangeSingleIteration() {
        eval("""
                      schema vm {
                        string name
                      }
                      for i in 5..6 {
                        resource vm main { name = '$i' }
                      }
                """);
        var schema = interpreter.getSchema("vm");
        assertEquals("5", interpreter.getInstance("main[5]").get("name"));
    }

    @Test
    void rangeEmpty_noInstances() {
        eval("""
                  schema vm { string name }
                  for i in 2..2 {
                    resource vm main { name = '$i' }
                  }
                """);
        var schema = interpreter.getSchema("vm");
        assertEquals(0, interpreter.getInstances().size());
    }

    @Test
    void arrayOfObjects_nestedProps() {
        eval("""
                  schema vm { string name }
                  var cfgs = [{meta: {client: "dev"}}, {meta: {client: "prod"}}]
                  for item, i in cfgs {
                    resource vm main { 
                        name = item.meta.client 
                    }
                  }
                """);
        var schema = interpreter.getSchema("vm");
        assertEquals("dev", interpreter.getInstance("main[0]").get("name"));
        assertEquals("prod", interpreter.getInstance("main[1]").get("name"));
    }

    @Test
    void stringKeyNamingFromArray() {
        eval("""
                  schema vm { string name }
                  for i in ["prod","test"] {
                    resource vm main { name = i }
                  }
                """);
        var schema = interpreter.getSchema("vm");
        assertEquals("prod", interpreter.getInstance("main[\"prod\"]").get("name"));
        assertEquals("test", interpreter.getInstance("main[\"test\"]").get("name"));
    }

    @Test
    void duplicateStringKeys_conflict() {
        // Expect either last-write-wins OR a specific exception; assert accordingly.
        assertThrows(DeclarationExistsException.class, () -> eval("""
                  schema vm { string name }
                  var items = ["dup","dup"]
                  for i in items {
                    resource vm main { name = i }
                  }
                """));
    }

    @Test
    void loopVarShadowsOuterVar() {
        eval("""
                  schema vm { string name }
                  var name = "outer"
                  for i in 0..1 {
                    var name = "inner"
                    resource vm main { name = name }
                  }
                """);
        var schema = interpreter.getSchema("vm");
        assertEquals("inner", interpreter.getInstance("main[0]").get("name"));
    }

    @Test
    void itemVarDoesNotCollideWithSchemaProps() {
        eval("""
                  schema vm { string name }
                  var items = [{name:"x"}, {name:"y"}]
                  for item, i in items {
                    resource vm main { name = item.name }
                  }
                """);
        assertEquals("x", interpreter.getInstance("main[0]").get("name"));
        assertEquals("y", interpreter.getInstance("main[1]").get("name"));
    }

    @Test
    void forwardReferenceAcrossOrderInLoop() {
        eval("""
                  schema vm { string name }
                  for i in 0..2 {
                    resource vm cidr { name = vpc.name }
                    resource vm vpc  { name = 'vpc-$i' }
                  }
                """);
        ResourceValue instance = interpreter.getInstance("cidr[0]");
        assertEquals("vpc-0", instance.get("name"));

        ResourceValue instance1 = interpreter.getInstance("cidr[1]");
        assertEquals("vpc-1", instance1.get("name"));
    }

    @Test
    void indirectCycleDetection() {
        assertThrows(CycleException.class, () -> eval("""
                  schema vm { string name }
                  for i in 0..1 {
                    resource vm a {  name = b.name }  // a -> b
                
                    resource vm b { name = a.name }  // b -> a
                  }
                """));
    }

    @Test
    void collectInstancesToArrayVar() {
        eval("""
                  schema vm { string name }
                  var vm[] col = []
                
                  for i in ["prod", "test"] {
                    resource vm main { name = i }
                    col += main
                  }
                """);
        var instance = interpreter.getInstance("main[\"prod\"]");
        assertNotNull(instance);
        assertEquals("prod", instance.get("name"));
        // Optionally assert col’s contents if your runtime exposes it
    }

    @Test
    void loopVarOutOfScope_error() {
        assertThrows(RuntimeException.class, () -> eval("""
                  schema vm { string name }
                  for i in 0..1 { resource vm main { name = 'ok' } }
                  resource vm late { name = i } // i not in scope
                """));
    }

    @Test
    void crossLoopIndexSpace_error() {
        assertThrows(RuntimeException.class, () -> eval("""
                  schema vm { string name }
                  for i in 0..2 { resource vm a { name = '$i' } }
                  for j in 0..2 { resource vm b { name = a[j].name } } // if bracket-indexing unsupported
                """));
    }

    @Test
    @Disabled("not implemented right now. We will support it later")
    void nestedLoops_captureBothIndices() {
        eval("""
                  schema vm { string name }
                  for i in 0..2 {
                    for j in 0..2 {
                      resource vm main { name = '$i-$j' }
                    }
                  }
                """);
        var schema = interpreter.getSchema("vm");
        assertEquals("0-0", interpreter.getInstance("main[0]").get("name"));
        assertEquals("0-1", interpreter.getInstance("main[1]").get("name"));
    }


    @Test
    void nestedLoopScopeRules() {
        eval("""
                  schema vm { string name }
                  var outer = "X"
                  for i in 0..1 {
                    var mid = "M"
                    for j in 0..1 {
                      resource vm main { name = '$outer-$mid-$j' }
                    }
                  }
                """);
        var schema = interpreter.getSchema("vm");
        assertEquals("X-M-0", interpreter.getInstance("main[0]").get("name"));
    }

    @Test
    void indexInNameAndProperty() {
        eval("""
                  schema vm { string name }
                  var items = ["a","b","c"]
                  for v, i in items {
                    resource vm main { name = '$v-$i' }
                  }
                """);
        var schema = interpreter.getSchema("vm");
        assertEquals("a-0", interpreter.getInstance("""
                main["a"]"""
                .trim()).get("name"));
        assertEquals("c-2", interpreter.getInstance("""
                main["c"]
                """.trim()).get("name"));
    }

    @Test
    @DisplayName("Resolve var name using NO quotes string interpolation inside if statement")
    void testForReturnsRsdesourceNestedVar() {
        var res = eval("""
                schema vm {
                   string name
                }
                for i in 0..2 {
                    var name = 'prod'
                    resource vm main {
                      name     = name
                    }
                }
                """);

        var schema = interpreter.getSchema("vm");

        var resource0 = interpreter.getInstance("main[0]");
        var resource1 = interpreter.getInstance("main[1]");

        assertInstanceOf(ResourceValue.class, resource0);
        assertInstanceOf(ResourceValue.class, resource1);
        assertEquals(resource1, res);
    }


    @Test
    @DisplayName("Multiple resources with resource name interpolation")
    void multiResourcesWithInterpolation() {
        eval("""
                schema vm {
                   string name
                }
                for i in 0..2 {
                    var name = 'prod'
                    resource vm main {
                      name     = '$name-$i'
                    }
                }
                """);

        assertEquals(2, interpreter.getInstances().size());

        assertEquals("prod-0", interpreter.getInstance("main[0]").get("name"));
        assertEquals("prod-1", interpreter.getInstance("main[1]").get("name"));
    }


    @Test
    @DisplayName("Multiple resources with dependencies")
    void multiResourcesWithDependencies() {
        eval("""
                schema vm {
                   string name
                }
                for i in 0..2 {
                    var name = 'prod'
                    resource vm cidr {
                      name     = vpc.name
                    }
                    resource vm vpc {
                      name     = '$name-$i'
                    }
                
                }
                """);

        var schema = interpreter.getSchema("vm");

        assertNotNull(schema);
//        assertEquals(4, schema.getArrays().size());

        ResourceValue actualValue = interpreter.getInstance("vpc[0]");
        assertInstanceOf(ResourceValue.class, actualValue);
        ResourceValue cidr0 = interpreter.getInstance("cidr[0]");
        ResourceValue cidr1 = interpreter.getInstance("cidr[1]");
        assertInstanceOf(ResourceValue.class, cidr1);
        assertEquals("prod-0", cidr0.get("name"));
        assertEquals("prod-1", cidr1.get("name"));
    }


    @Test
    @DisplayName("Multiple resources with dependencies")
    void dependsOnEarlyResource() {
        eval("""
                schema vm {
                   string name
                }
                for i in 0..2 {
                    var name = 'prod'
                
                    resource vm vpc {
                      name     = '$name-$i'
                    }
                    resource vm cidr {
                      name     = vpc.name
                    }
                }
                """);

        var schema = interpreter.getSchema("vm");

        assertNotNull(schema);
//        assertEquals(4, schema.getArrays().size());

        ResourceValue vpc0 = interpreter.getInstance("vpc[0]");
        assertInstanceOf(ResourceValue.class, vpc0);
        ResourceValue cidr0 = interpreter.getInstance("cidr[0]");
        ResourceValue cidr1 = interpreter.getInstance("cidr[1]");
        assertEquals("prod-0", cidr0.get("name"));
        assertEquals("prod-1", cidr1.get("name"));
    }


    @Test
    @DisplayName("Multiple resources with dependencies")
    void dependsOnEarlyResourceCycle() {
        assertThrows(CycleException.class, () -> eval("""
                schema vm {
                   string name
                   string color 
                }
                for i in 0..2 {
                    var name = 'prod'
                
                    resource vm vpc {
                      name     = '$name-$i'
                      color    = cidr.color 
                    }
                    resource vm cidr {
                      name     = vpc.name
                      color    = vpc.color
                    }
                
                }
                """));
    }

    @Test
    @DisplayName("Create multiple resources in a loop")
    void testMultipleResourcesAreCreatedForLoop() {
        var res = eval("""
                schema vm {
                   string name
                }
                var vm[] vms = []
                for i in 0..2 {
                    var name = 'prod'
                    resource vm main {
                      name     = name
                    }
                    vms += main
                }
                """);


        assertEquals(2, interpreter.getInstances().size());
        assertEquals("prod", interpreter.getInstance("main[0]").get("name"));
        assertEquals("prod", interpreter.getInstance("main[1]").get("name"));
    }

    @Test
    @DisplayName("Create multiple resources in a loop by string interpolation")
    void testMultipleResourcesAreCreatedForLoopUsingStringInterpolation() {
        var res = eval("""
                schema vm {
                   string name
                }
                var vm[] vms = []
                var items = ['prod','test']
                for i in items {
                    var name = 'prod'
                    resource vm main {
                      name     = name
                    }
                    vms += main
                }
                """);

        assertEquals(2, interpreter.getInstances().size());
        assertEquals("prod", interpreter.getInstance("""
                main["prod"]""").get("name"));
        assertEquals("prod", interpreter.getInstance("""
                main["test"]""").get("name"));
    }

    @Test
    @DisplayName("Print list of resources")
    void printListOfResources() {
        var res = eval("""
                schema vm {
                   string name
                }
                resource vm main {
                      name     = "main"
                }
                resource vm second {
                      name     = "second"
                }
                
                for it in [main, second] {
                    println(it)
                }
                """);
    }

    @Test
    @DisplayName("Create multiple resources in a loop by string interpolation")
    void testForLoopInlineArray() {
        var res = eval("""
                schema vm {
                   string name
                }
                var vm[] vms = []
                for i in ['prod','test'] {
                    var name = 'prod'
                    resource vm main {
                      name     = name
                    }
                    vms += main
                }
                """);

        assertEquals(2, interpreter.getInstances().size());
        assertEquals("prod", interpreter.getInstance("""
                main["prod"]""").get("name"));
        assertEquals("prod", interpreter.getInstance("""
                main["test"]""").get("name"));
    }

    @Test
    @DisplayName("Create multiple resources in a loop by using index")
    void testMultipleResourcesAreCreatedForLoopUsingIndex() {
        var res = eval("""
                schema vm {
                   string name
                }
                var vm[] vms = []
                var items = ['prod','test']
                for i in items {
                    resource vm main {
                      name     = i
                    }
                    vms += main
                }
                """);

        assertEquals(2, interpreter.getInstances().size());
        assertEquals("prod", interpreter.getInstance("""
                main["prod"]""").get("name"));
        assertEquals("test", interpreter.getInstance("""
                main["test"]""").get("name"));
    }

    @Test
    @DisplayName("Create multiple resources in a loop by using index")
    void testMultipleResourcesAreAccessedUsingIndexSyntaxStrings() {
        eval("""
                schema vm {
                   string name
                }
                var vm[] vms = []
                var items = ['prod','test']
                for i in items {
                    resource vm main {
                      name     = i
                    }
                    vms += main[i]
                }
                """);

        assertEquals(2, interpreter.getInstances().size());

        var prod = interpreter.getInstance("main[\"prod\"]");
        var test = interpreter.getInstance("main[\"test\"]");

        var schema = interpreter.getSchema("vm");

        // Verify prod resource
        assertEquals("main", prod.getName());
        assertEquals("prod", prod.get("name"));
        assertEquals(schema, prod.getSchema());
        assertEquals("vm.main[\"prod\"]", prod.getPath().toDatabaseKey());

        // Verify test resource
        assertEquals("main", test.getName());
        assertEquals("test", test.get("name"));
        assertEquals(schema, test.getSchema());
        assertEquals("vm.main[\"test\"]", test.getPath().toDatabaseKey());
    }

    @Test
    @DisplayName("resource naming by string array ")
    void testResourceNamingByStringArray() {
        eval("""
                schema vm {
                   string name
                }
                for i in ['prod','test'] {
                    resource vm main {
                      name     = i
                    }
                }
                """);

        assertEquals(2, interpreter.getInstances().size());
        assertEquals("prod", interpreter.getInstance("main[\"prod\"]").get("name"));
        assertEquals("main", interpreter.getInstance("main[\"prod\"]").getName());
        assertEquals("test", interpreter.getInstance("main[\"test\"]").get("name"));
    }

    @Test
    @DisplayName("for loop over objects to create resources")
    void forResourceObjects() {
        var res = eval("""
                schema vm {
                   string name
                }
                var configs = [
                     { name: "photos", location: "eu-west1" },
                     { name: "videos", location: "us-east1" }
                ]
                
                for cfg in configs {
                    resource vm cfg.name {
                        name = cfg.location
                    }
                }
                """);

        var videos = interpreter.getInstance("videos");
        var photos = interpreter.getInstance("photos");

        assertNotNull(videos);
        assertNotNull(photos);
        assertEquals(videos, res);
    }

    @Test
    @DisplayName("for loop over objects to create resources names as strings")
    @Disabled("todo. in a future language version")
    void forResourceObjectsAsStrings() {
        var res = eval("""
                schema vm {
                   string name
                }
                var configs = [
                     { name: "photos", location: "eu-west1" },
                     { name: "videos", location: "us-east1" }
                ]
                
                for cfg in configs {
                    resource vm "main-${cfg.name}-${cfg.location}" {
                        name = cfg.location
                    }
                }
                """);

        var schema = interpreter.getSchema("vm");

        var videos = interpreter.getInstance("videos");
        var photos = interpreter.getInstance("photos");

        assertNotNull(videos);
        assertNotNull(photos);
        assertEquals(videos, res);
    }

    @Test
    @DisplayName("for loop comprehension over objects with resource names as object properties")
    void arrayResourcesOverObjectsWithResourceNamesFromObjects() {
        eval("""
                schema Bucket {
                   string name
                }
                var envs = [{client: 'amazon'}, {client: 'bmw'}]
                [for index in envs]
                resource Bucket index.client {
                  name     = 'name-${index.client}'
                }
                """);

        var instances = interpreter.getInstances();
        assertEquals("name-amazon", instances.get("amazon").get("name"));
        assertEquals("name-bmw", instances.get("bmw").get("name"));
    }

    @Test
    @DisplayName("for loop comprehension over objects with resource names as strings")
    @Disabled("todo. in future version")
    void arrayResourcesOverObjectsResourceNameAsString() {
        var array = eval("""
                schema Bucket {
                   string name
                }
                var envs = [{client: 'amazon'}, {client: 'bmw'}]
                [for index in envs]
                resource Bucket "main-${index.client}" {
                  name     = 'name-${index.client}'
                }
                """);

        var map = new HashMap<String, ResourceValue>();
        var schemaValue = (SchemaValue) this.interpreter.getEnv().get("Bucket");
        map.put("main-amazon", resourceValue("main-amazon", new Environment<>(Map.of("name", "name-amazon")), schemaValue));
        map.put("main-bmw", resourceValue("main-bmw", new Environment<>(Map.of("name", "name-bmw")), schemaValue));
        assertEquals(map, interpreter.getInstances());
    }

    @Test
    void arrayResourcesOverNumbers() {
        var array = eval("""
                schema Bucket {
                   string name
                }
                var envs = [1,2,3]
                [for index in envs]
                resource Bucket photos {
                  name     = 'name-${index}'
                }
                """);
        var instances = interpreter.getInstances();
        assertEquals("name-1", instances.get("photos[1]").get("name"));
        assertEquals("name-2", instances.get("photos[2]").get("name"));
        assertEquals("name-3", instances.get("photos[3]").get("name"));
    }

    @Test
    void arrayResourcesOverStrings() {
        var array = eval("""
                schema Bucket {
                   string name
                }
                var envs = ['hello', 'world']
                [for index in envs]
                resource Bucket photos {
                  name     = 'name-${index}'
                }
                """);

        var instances = interpreter.getInstances();
        assertEquals("name-hello", instances.get("photos[\"hello\"]").get("name"));
        assertEquals("name-world", instances.get("photos[\"world\"]").get("name"));
    }

    @Test
    @DisplayName("Create multiple resources in a loop by using numeric index")
    void testMultipleResourcesAreAccessedUsingIndexSyntaxNumbers() {
        eval("""
                schema vm {
                   string name
                   int id
                }
                var vm[] vms = []
                var items = [0, 1, 2]
                for i in items {
                    resource vm main {
                      name = "server-${i}"
                      id   = i
                    }
                    vms += main[i]
                }
                """);

        assertEquals(3, interpreter.getInstances().size());

        var main0 = interpreter.getInstance("main[0]");
        assertEquals("server-0", main0.get("name"));
        assertEquals(0, main0.get("id"));

        var main1 = interpreter.getInstance("main[1]");
        assertEquals("server-1", main1.get("name"));
        assertEquals(1, main1.get("id"));

        var main2 = interpreter.getInstance("main[2]");
        assertEquals("server-2", main2.get("name"));
        assertEquals(2, main2.get("id"));
    }

    @Test
    @DisplayName("Create multiple resources in a loop using object properties as index")
    void testMultipleResourcesAreAccessedUsingObjectIndex() {
        eval("""
                schema vm {
                   string name
                   string environment
                   string region
                }
                var vm[] vms = []
                var configs = [
                    {env: "prod", region: "us-east"},
                    {env: "dev", region: "us-west"}
                ]
                for config in configs {
                    resource vm main {
                      name        = "server-${config.env}"
                      environment = config.env
                      region      = config.region
                    }
                    vms += main
                }
                """);

        assertEquals(2, interpreter.getInstances().size());

        var prodInstance = interpreter.getInstance("main[\"{env=prod, region=us-east}\"]");
        assertNotNull(prodInstance);
        assertEquals("server-prod", prodInstance.get("name"));
        assertEquals("prod", prodInstance.get("environment"));
        assertEquals("us-east", prodInstance.get("region"));

        var devInstance = interpreter.getInstance("main[\"{env=dev, region=us-west}\"]");
        assertNotNull(devInstance);
        assertEquals("server-dev", devInstance.get("name"));
        assertEquals("dev", devInstance.get("environment"));
        assertEquals("us-west", devInstance.get("region"));
    }

    @Test
    @DisplayName("Create multiple resources with mixed numeric and string indices")
    void testMultipleResourcesWithMixedIndices() {
        var res = eval("""
                schema vm {
                   string name
                }
                var vm[] vms = []
                
                // Create with string indices
                for env in ["prod", "staging"] {
                    resource vm web {
                      name = "web-${env}"
                    }
                    vms += web[env]
                }
                
                // Create with numeric indices
                for i in [0, 1, 2] {
                    resource vm db {
                      name = "db-${i}"
                    }
                    vms += db[i]
                }
                """);

        assertEquals(5, interpreter.getInstances().size());

        // Verify string-indexed resources
        assertEquals("web-prod", interpreter.getInstance("""
                web["prod"]""").get("name"));
        assertEquals("web-staging", interpreter.getInstance("""
                web["staging"]""").get("name"));

        // Verify numeric-indexed resources
        assertEquals("db-0", interpreter.getInstance("db[0]").get("name"));
        assertEquals("db-1", interpreter.getInstance("db[1]").get("name"));
        assertEquals("db-2", interpreter.getInstance("db[2]").get("name"));
    }


}
