package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import cloud.kitelang.execution.values.ResourceValue;
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
                   string name
                }
                if true {
                    var name = 'prod'
                    resource vm main {
                      name     = '$name'
                    }
                }
                """);

        var resource = interpreter.getInstance("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
    }


    @Test
    @DisplayName("Resolve var name using double quotes string interpolation inside if statement")
    void testInterpolationNestedInIf() {
        var res = eval("""
                schema vm {
                   string name
                }
                if true {
                    var name = 'prod'
                    resource vm main {
                      name     = "$name"
                    }
                }
                """);

        var resource = interpreter.getInstance("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
        assertEquals("prod", resource.argVal("name"));
    }


    @Test
    @DisplayName("Resolve var name using double quotes string interpolation inside if statement")
    void testInterpolationNestedDeepInIf() {
        var res = eval("""
                schema vm {
                   string name
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

        var resource = interpreter.getInstance("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
        assertEquals("prod", resource.argVal("name"));
    }
    @Test
    @DisplayName("Resolve var name using NO quotes string interpolation inside if statement")
    void testIfConditionReturnsResourceNestedVar() {
        var res = eval("""
                schema vm {
                   string name
                }
                if true {
                    var name = 'prod'
                    resource vm main {
                      name     = name
                    }
                }
                """);

        var resource = interpreter.getInstance("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
    }

    @Test
    void testIfConditionReturnsResource() {
        var res = eval("""
                schema vm {
                   string name
                }
                if true {
                    resource vm main {
                      name     = 'prod'
                    }
                }
                """);

        var resource = interpreter.getInstance("main");

        assertInstanceOf(ResourceValue.class, resource);
        assertEquals(resource, res);
    }
}
