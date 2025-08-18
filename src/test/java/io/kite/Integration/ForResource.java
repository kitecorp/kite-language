package io.kite.Integration;

import io.kite.Base.RuntimeTest;
import io.kite.Runtime.CycleException;
import io.kite.Runtime.Values.ResourceValue;
import io.kite.Runtime.Values.SchemaValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ForResource extends RuntimeTest {
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

        var resource = schema.getInstances().get("main[0]");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
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
}
