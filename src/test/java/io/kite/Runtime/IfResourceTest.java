package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import io.kite.Runtime.Values.ResourceValue;
import io.kite.Runtime.Values.SchemaValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class IfResourceTest extends RuntimeTest {
    @Test
    @DisplayName("Resolve var name using single quotes string interpolation inside if statement")
    void testIfConditionReturnsResourceNestedVarStringInterpolation() {
        var res = eval("""
                schema vm {
                   var string name
                }
                if true {
                    var name = 'prod'
                    resource vm main {
                      name     = '$name'
                    }
                }
                """);

        var schema = (SchemaValue) global.get("vm");

        var resource = schema.getInstances().get("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
    }


    @Test
    @DisplayName("Resolve var name using double quotes string interpolation inside if statement")
    void testInterpolationNestedInIf() {
        var res = eval("""
                schema vm {
                   var string name
                }
                if true {
                    var name = 'prod'
                    resource vm main {
                      name     = "$name"
                    }
                }
                """);

        var schema = (SchemaValue) global.get("vm");

        var resource = schema.getInstances().get("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
        assertEquals("prod", resource.argVal("name"));
    }


    @Test
    @DisplayName("Resolve var name using double quotes string interpolation inside if statement")
    void testInterpolationNestedDeepInIf() {
        var res = eval("""
                schema vm {
                   var string name
                }
                if true {
                    var name = 'prod'
                    if true {
                        resource vm main {
                          name     = "$name"
                        }
                    }
                }
                """);

        var schema = (SchemaValue) global.get("vm");

        var resource = schema.getInstances().get("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
        assertEquals("prod", resource.argVal("name"));
    }
    @Test
    @DisplayName("Resolve var name using NO quotes string interpolation inside if statement")
    void testIfConditionReturnsResourceNestedVar() {
        var res = eval("""
                schema vm {
                   var string name
                }
                if true {
                    var name = 'prod'
                    resource vm main {
                      name     = name
                    }
                }
                """);

        var schema = (SchemaValue) global.get("vm");

        var resource = schema.getInstances().get("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
    }

    @Test
    void testIfConditionReturnsResource() {
        var res = eval("""
                schema vm {
                   var string name
                }
                if true {
                    resource vm main {
                      name     = 'prod'
                    }
                }
                """);

        var schema = (SchemaValue) global.get("vm");

        var resource = schema.getInstances().get("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
    }
}
