package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.Runtime.Environment.Environment;
import io.kite.Runtime.Values.ResourceValue;
import io.kite.Runtime.Values.SchemaValue;
import io.kite.Runtime.exceptions.DeclarationExistsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.kite.Runtime.Values.ResourceValue.resourceValue;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("For loop resources")
public class ForResourceTest extends RuntimeTest {
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
                  for i, item in cfgs {
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
        Assertions.assertThrows(DeclarationExistsException.class, () -> eval("""
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
                  for i, item in items {
                    resource vm main { name = item.name }
                  }
                """);
        var schema = interpreter.getSchema("vm");
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
        var schema = interpreter.getSchema("vm");
        assertEquals("vpc-0", interpreter.getInstance("cidr[0]").get("name"));
        assertEquals("vpc-1", interpreter.getInstance("cidr[1]").get("name"));
    }

    @Test
    void indirectCycleDetection() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
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
        assertEquals("prod", interpreter.getInstance("main[\"prod\"]").get("name"));
        // Optionally assert col’s contents if your runtime exposes it
    }

    @Test
    void loopVarOutOfScope_error() {
        Assertions.assertThrows(RuntimeException.class, () -> eval("""
                  schema vm { string name }
                  for i in 0..1 { resource vm main { name = 'ok' } }
                  resource vm late { name = i } // i not in scope
                """));
    }

    @Test
    void crossLoopIndexSpace_error() {
        Assertions.assertThrows(RuntimeException.class, () -> eval("""
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
                  for i, v in items {
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
        Assertions.assertThrows(CycleException.class, () -> eval("""
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
    @DisplayName("resource naming by string array ")
    void testResourceNamingByStringArray() {
        var res = eval("""
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
        assertEquals("prod", interpreter.getInstance("""
                main["prod"]
                """.trim()).get("name"));
        assertEquals("main[\"prod\"]", interpreter.getInstance("""
                main["prod"]
                """.trim()).getName());
        assertEquals("test", interpreter.getInstance("""
                main["test"]
                """.trim()).get("name"));
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

        var schema = interpreter.getSchema("vm");

        var videos = interpreter.getInstance("videos");
        var photos = interpreter.getInstance("photos");

        Assertions.assertNotNull(videos);
        Assertions.assertNotNull(photos);
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

        Assertions.assertNotNull(videos);
        Assertions.assertNotNull(photos);
        assertEquals(videos, res);
    }

    @Test
    @DisplayName("for loop comprehension over objects with resource names as object properties")
    void arrayResourcesOverObjectsWithResourceNamesFromObjects() {
        var array = eval("""
                schema Bucket {
                   string name
                }
                var envs = [{client: 'amazon'}, {client: 'bmw'}]
                [for index in envs]
                resource Bucket index.client {
                  name     = 'name-${index.client}'
                }
                """);

        var map = new HashMap<String, ResourceValue>();
        var schemaValue = (SchemaValue) this.interpreter.getEnv().get("Bucket");
        map.put("amazon", resourceValue("amazon", new Environment<>(Map.of("name", "name-amazon")), schemaValue));
        map.put("bmw", resourceValue("bmw", new Environment<>(Map.of("name", "name-bmw")), schemaValue));
        assertEquals(map, interpreter.getInstances());
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
        var map = new LinkedHashMap<String, ResourceValue>();
        var schemaValue = this.interpreter.getSchema("Bucket");
        map.put("photos[1]",  resourceValue("photos[1]", new Environment<>(Map.of("name", "name-1")), schemaValue));
        map.put("photos[2]",  resourceValue("photos[2]", new Environment<>(Map.of("name", "name-2")), schemaValue));
        map.put("photos[3]",  resourceValue("photos[3]", new Environment<>(Map.of("name", "name-3")), schemaValue));
        assertEquals(map, interpreter.getInstances());
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

        var map = new HashMap<String, ResourceValue>();
        var schemaValue = (SchemaValue) this.interpreter.getEnv().get("Bucket");
        map.put("photos[\"hello\"]",  resourceValue("photos[\"hello\"]", new Environment<>(Map.of("name", "name-hello")), schemaValue));
        map.put("photos[\"world\"]",  resourceValue("photos[\"world\"]", new Environment<>(Map.of("name", "name-world")), schemaValue));
        assertEquals(map, interpreter.getInstances());
    }


}
