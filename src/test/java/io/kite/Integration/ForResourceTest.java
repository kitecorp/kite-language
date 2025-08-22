package io.kite.Integration;

import io.kite.Base.RuntimeTest;
import io.kite.Runtime.CycleException;
import io.kite.Runtime.Values.ResourceValue;
import io.kite.Runtime.Values.SchemaValue;
import io.kite.Runtime.exceptions.DeclarationExistsException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("For loop resources")
public class ForResourceTest extends RuntimeTest {
    @Test
    @DisplayName("Resolve var name using NO quotes string interpolation inside if statement")
    void testForReturnsResourceNestedVar() {
        var res = eval("""
                schema vm {
                   var string name
                }
                for i in 0..2 {
                    var name = 'prod'
                    resource vm main {
                      name     = name
                    }
                }
                """);

        var schema = (SchemaValue) global.get("vm");

        var resource0 = schema.getInstances().get("main[0]");
        var resource1 = schema.getInstances().get("main[1]");

        assertInstanceOf(ResourceValue.class, resource0);
        assertInstanceOf(ResourceValue.class, resource1);
        assertEquals(resource1, res);
    }

    @Test
    @DisplayName("Test multiple resources")
    void multipleResources() {
        var res = eval("""
                schema vm {
                   var string name
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

        var schema = (SchemaValue) global.get("vm");

        var resource0 = schema.getInstances().get("main[0]");
        var resource1 = schema.getInstances().get("main[1]");
        assertInstanceOf(ResourceValue.class, resource0);
        assertInstanceOf(ResourceValue.class, resource1);

        var second0 = schema.getInstances().get("second[0]");
        var second1 = schema.getInstances().get("second[1]");
        assertInstanceOf(ResourceValue.class, second0);
        assertInstanceOf(ResourceValue.class, second1);
    }

    @Test
    void rangeSingleIteration() {
        eval("""
                      schema vm {
                        var string name
                      }
                      for i in 5..6 {
                        resource vm main { name = '$i' }
                      }
                """);
        var schema = (SchemaValue) global.get("vm");
        assertEquals("5", schema.getInstances().get("main[5]").get("name"));
    }

    @Test
    void rangeEmpty_noInstances() {
        eval("""
                  schema vm { var string name }
                  for i in 2..2 {
                    resource vm main { name = '$i' }
                  }
                """);
        var schema = (SchemaValue) global.get("vm");
        assertEquals(0, schema.getInstances().size());
    }

    @Test
    void arrayOfObjects_nestedProps() {
        eval("""
                  schema vm { var string name }
                  var cfgs = [{meta: {client: "dev"}}, {meta: {client: "prod"}}]
                  for i, item in cfgs {
                    resource vm main { 
                        name = item.meta.client 
                    }
                  }
                """);
        var schema = (SchemaValue) global.get("vm");
        assertEquals("dev", schema.getInstances().get("main[0]").get("name"));
        assertEquals("prod", schema.getInstances().get("main[1]").get("name"));
    }

    @Test
    void stringKeyNamingFromArray() {
        eval("""
                  schema vm { var string name }
                  for i in ["prod","test"] {
                    resource vm main { name = i }
                  }
                """);
        var schema = (SchemaValue) global.get("vm");
        assertEquals("prod", schema.getInstances().get("main[\"prod\"]").get("name"));
        assertEquals("test", schema.getInstances().get("main[\"test\"]").get("name"));
    }

    @Test
    void duplicateStringKeys_conflict() {
        // Expect either last-write-wins OR a specific exception; assert accordingly.
        Assertions.assertThrows(DeclarationExistsException.class, () -> eval("""
                  schema vm { var string name }
                  var items = ["dup","dup"]
                  for i in items {
                    resource vm main { name = i }
                  }
                """));
    }

    @Test
    void loopVarShadowsOuterVar() {
        eval("""
                  schema vm { var string name }
                  var name = "outer"
                  for i in 0..1 {
                    var name = "inner"
                    resource vm main { name = name }
                  }
                """);
        var schema = (SchemaValue) global.get("vm");
        assertEquals("inner", schema.getInstances().get("main[0]").get("name"));
    }

    @Test
    void itemVarDoesNotCollideWithSchemaProps() {
        eval("""
                  schema vm { var string name }
                  var items = [{name:"x"}, {name:"y"}]
                  for i, item in items {
                    resource vm main { name = item.name }
                  }
                """);
        var schema = (SchemaValue) global.get("vm");
        assertEquals("x", schema.getInstances().get("main[0]").get("name"));
        assertEquals("y", schema.getInstances().get("main[1]").get("name"));
    }

    @Test
    void forwardReferenceAcrossOrderInLoop() {
        eval("""
                  schema vm { var string name }
                  for i in 0..2 {
                    resource vm cidr { name = vm.vpc.name }
                    resource vm vpc  { name = 'vpc-$i' }
                  }
                """);
        var schema = (SchemaValue) global.get("vm");
        assertEquals("vpc-0", schema.getInstances().get("cidr[0]").get("name"));
        assertEquals("vpc-1", schema.getInstances().get("cidr[1]").get("name"));
    }

    @Test
    void indirectCycleDetection() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                  schema vm { var string name }
                  for i in 0..1 {
                    resource vm a {  name = vm.b.name }  // a -> b
                
                    resource vm b { name = vm.a.name }  // b -> a
                  }
                """));
    }

    @Test
    void collectInstancesToArrayVar() {
        eval("""
                  schema vm { var string name }
                  var vm[] col = []
                  for i in ["prod","test"] {
                    resource vm main { name = i }
                    col += vm.main
                  }
                """);
        var schema = (SchemaValue) global.get("vm");
        assertEquals("prod", schema.getInstances().get("main[\"prod\"]").get("name"));
        // Optionally assert col’s contents if your runtime exposes it
    }

    @Test
    void loopVarOutOfScope_error() {
        Assertions.assertThrows(RuntimeException.class, () -> eval("""
                  schema vm { var string name }
                  for i in 0..1 { resource vm main { name = 'ok' } }
                  resource vm late { name = i } // i not in scope
                """));
    }

    @Test
    void crossLoopIndexSpace_error() {
        Assertions.assertThrows(RuntimeException.class, () -> eval("""
                  schema vm { var string name }
                  for i in 0..2 { resource vm a { name = '$i' } }
                  for j in 0..2 { resource vm b { name = vm.a[j].name } } // if bracket-indexing unsupported
                """));
    }

    @Test
    @Disabled("not implemented right now. We will support it later")
    void nestedLoops_captureBothIndices() {
        eval("""
                  schema vm { var string name }
                  for i in 0..2 {
                    for j in 0..2 {
                      resource vm main { name = '$i-$j' }
                    }
                  }
                """);
        var schema = (SchemaValue) global.get("vm");
        assertEquals("0-0", schema.getInstances().get("main[0]").get("name"));
        assertEquals("0-1", schema.getInstances().get("main[1]").get("name"));
    }


    @Test
    void nestedLoopScopeRules() {
        eval("""
                  schema vm { var string name }
                  var outer = "X"
                  for i in 0..1 {
                    var mid = "M"
                    for j in 0..1 {
                      resource vm main { name = '$outer-$mid-$j' }
                    }
                  }
                """);
        var schema = (SchemaValue) global.get("vm");
        assertEquals("X-M-0", schema.getInstances().get("main[0]").get("name"));
    }

    @Test
    void indexInNameAndProperty() {
        eval("""
                  schema vm { var string name }
                  var items = ["a","b","c"]
                  for i, v in items {
                    resource vm main { name = '$v-$i' }
                  }
                """);
        var schema = (SchemaValue) global.get("vm");
        assertEquals("a-0", schema.getInstances().get("""
                main["a"]"""
                .trim()).get("name"));
        assertEquals("c-2", schema.getInstances().get("""
                main["c"]
                """.trim()).get("name"));
    }

    @Test
    @DisplayName("Resolve var name using NO quotes string interpolation inside if statement")
    void testForReturnsRsdesourceNestedVar() {
        var res = eval("""
                schema vm {
                   var string name
                }
                for i in 0..2 {
                    var name = 'prod'
                    resource vm main {
                      name     = name
                    }
                }
                """);

        var schema = (SchemaValue) global.get("vm");

        var resource0 = schema.getInstances().get("main[0]");
        var resource1 = schema.getInstances().get("main[1]");

        assertInstanceOf(ResourceValue.class, resource0);
        assertInstanceOf(ResourceValue.class, resource1);
        assertEquals(resource1, res);
    }


    @Test
    @DisplayName("Multiple resources with resource name interpolation")
    void multiResourcesWithInterpolation() {
        eval("""
                schema vm {
                   var string name
                }
                for i in 0..2 {
                    var name = 'prod'
                    resource vm main {
                      name     = '$name-$i'
                    }
                }
                """);

        var schema = (SchemaValue) global.get("vm");

        assertNotNull(schema);
        assertEquals(2, schema.getInstances().size());

        assertEquals("prod-0", schema.getInstances().get("main[0]").get("name"));
        assertEquals("prod-1", schema.getInstances().get("main[1]").get("name"));
    }


    @Test
    @DisplayName("Multiple resources with dependencies")
    void multiResourcesWithDependencies() {
        eval("""
                schema vm {
                   var string name
                }
                for i in 0..2 {
                    var name = 'prod'
                    resource vm cidr {
                      name     = vm.vpc.name
                    }
                    resource vm vpc {
                      name     = '$name-$i'
                    }
                
                }
                """);

        var schema = (SchemaValue) global.get("vm");

        assertNotNull(schema);
//        assertEquals(4, schema.getArrays().size());

        var vpcs = schema.getInstances();
        var cidr = schema.getInstances();
        ResourceValue actualValue = vpcs.get("vpc[0]");
        assertInstanceOf(ResourceValue.class, actualValue);
        ResourceValue cidr0 = cidr.get("cidr[0]");
        ResourceValue cidr1 = cidr.get("cidr[1]");
        assertInstanceOf(ResourceValue.class, cidr1);
        assertEquals("prod-0", cidr0.get("name"));
        assertEquals("prod-1", cidr1.get("name"));
    }


    @Test
    @DisplayName("Multiple resources with dependencies")
    void dependsOnEarlyResource() {
        eval("""
                schema vm {
                   var string name
                }
                for i in 0..2 {
                    var name = 'prod'
                
                    resource vm vpc {
                      name     = '$name-$i'
                    }
                    resource vm cidr {
                      name     = vm.vpc.name
                    }
                
                }
                """);

        var schema = (SchemaValue) global.get("vm");

        assertNotNull(schema);
//        assertEquals(4, schema.getArrays().size());

        var vpcs = schema.getInstances();
        var cidr = schema.getInstances();
        ResourceValue actualValue = vpcs.get("vpc[0]");
        assertInstanceOf(ResourceValue.class, actualValue);
        ResourceValue cidr0 = cidr.get("cidr[0]");
        ResourceValue cidr1 = cidr.get("cidr[1]");
        assertEquals("prod-0", cidr0.get("name"));
        assertEquals("prod-1", cidr1.get("name"));
    }


    @Test
    @DisplayName("Multiple resources with dependencies")
    void dependsOnEarlyResourceCycle() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm {
                   var string name
                   var string color 
                }
                for i in 0..2 {
                    var name = 'prod'
                
                    resource vm vpc {
                      name     = '$name-$i'
                      color    = vm.cidr.color 
                    }
                    resource vm cidr {
                      name     = vm.vpc.name
                      color    = vm.vpc.color
                    }
                
                }
                """));
    }

    @Test
    @DisplayName("Create multiple resources in a loop")
    void testMultipleResourcesAreCreatedForLoop() {
        var res = eval("""
                schema vm {
                   var string name
                }
                var vm[] vms = []
                for i in 0..2 {
                    var name = 'prod'
                    resource vm main {
                      name     = name
                    }
                    vms += vm.main
                }
                """);

        var schema = (SchemaValue) global.get("vm");


        var arrays = schema.getInstances();
        assertEquals(2, arrays.size());
        assertEquals("prod", arrays.get("main[0]").get("name"));
        assertEquals("prod", arrays.get("main[1]").get("name"));
    }

    @Test
    @DisplayName("Create multiple resources in a loop by string interpolation")
    void testMultipleResourcesAreCreatedForLoopUsingStringInterpolation() {
        var res = eval("""
                schema vm {
                   var string name
                }
                var vm[] vms = []
                var items = ['prod','test']
                for i in items {
                    var name = 'prod'
                    resource vm main {
                      name     = name
                    }
                    vms += vm.main
                }
                """);

        var schema = (SchemaValue) global.get("vm");


        var arrays = schema.getInstances();
        assertEquals(2, arrays.size());
        assertEquals("prod", arrays.get("""
                main["prod"]""").get("name"));
        assertEquals("prod", arrays.get("""
                main["test"]""").get("name"));
    }

    @Test
    @DisplayName("Create multiple resources in a loop by string interpolation")
    void testForLoopInlineArray() {
        var res = eval("""
                schema vm {
                   var string name
                }
                var vm[] vms = []
                for i in ['prod','test'] {
                    var name = 'prod'
                    resource vm main {
                      name     = name
                    }
                    vms += vm.main
                }
                """);

        var schema = (SchemaValue) global.get("vm");


        var arrays = schema.getInstances();
        assertEquals(2, arrays.size());
        assertEquals("prod", arrays.get("""
                main["prod"]""").get("name"));
        assertEquals("prod", arrays.get("""
                main["test"]""").get("name"));
    }

    @Test
    @DisplayName("Create multiple resources in a loop by using index")
    void testMultipleResourcesAreCreatedForLoopUsingIndex() {
        var res = eval("""
                schema vm {
                   var string name
                }
                var vm[] vms = []
                var items = ['prod','test']
                for i in items {
                    resource vm main {
                      name     = i
                    }
                    vms += vm.main
                }
                """);

        var schema = (SchemaValue) global.get("vm");


        var arrays = schema.getInstances();
        assertEquals(2, arrays.size());
        assertEquals("prod", arrays.get("""
                main["prod"]""").get("name"));
        assertEquals("test", arrays.get("""
                main["test"]""").get("name"));
    }

    @Test
    @DisplayName("resource naming by string array ")
    void testResourceNamingByStringArray() {
        var res = eval("""
                schema vm {
                   var string name
                }
                for i in ['prod','test'] {
                    resource vm main {
                      name     = i
                    }
                }
                """);

        var schema = (SchemaValue) global.get("vm");


        var arrays = schema.getInstances();
        assertEquals(2, arrays.size());
        assertEquals("prod", arrays.get("""
                main["prod"]
                """.trim()).get("name"));
        assertEquals("main[\"prod\"]", arrays.get("""
                main["prod"]
                """.trim()).getName());
        assertEquals("test", arrays.get("""
                main["test"]
                """.trim()).get("name"));
    }

    @Test
    @DisplayName("resource naming by for loop")
    @Disabled("doesn't make sense to implement this because it's difficult to reference the resource later. vm.cfg.name? vm.photos? it's not even defined in code so how does it make sense?")
    void testForNameResource() {
        var res = eval("""
                schema vm {
                   var string name
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

        var schema = (SchemaValue) global.get("vm");

        var resource = schema.getInstances().get("main[0]");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
    }
}
