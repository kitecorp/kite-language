package cloud.kitelang.execution.decorators;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * All the tests from print but with added sensitive values.
 */
@Slf4j
@DisplayName("TypeChecker @count")
public class CountTests extends DecoratorTests {

    @Test
    void outputSensitive() {
        eval("""
                @sensitive
                output string something = "a"
                """);
    }

    @Test
    void countResource() {
        eval("""
                schema vm { string name }
                
                @count(2)
                resource vm main {
                
                }
                """);
    }

    @Test
    void countResourceStringInterpolation() {
        eval("""
                schema vm { string name }
                
                @count(2)
                resource vm main {
                    name = "main-$count"
                }
//                var x = main[0].name
                """);
        var main0 = interpreter.getInstance("main[0]");
        var main1 = interpreter.getInstance("main[1]");
        Assertions.assertNotNull(main0);
        Assertions.assertNotNull(main1);
        Assertions.assertEquals("main-0", main0.get("name"));
        Assertions.assertEquals("main-1", main1.get("name"));
    }

    @Test
    void countResourceStringInterpolationAlternative() {
        eval("""
                schema vm { string name }
                
                @count(2)
                resource vm main {
                    name = "main-${count}"
                }
                """);
        var main0 = interpreter.getInstance("main[0]");
        var main1 = interpreter.getInstance("main[1]");
        Assertions.assertNotNull(main0);
        Assertions.assertNotNull(main1);
        Assertions.assertEquals("main-0", main0.get("name"));
        Assertions.assertEquals("main-1", main1.get("name"));
    }

    @Test
    void countResourceDependencyImplicitIndex() {
        eval("""
                schema vm { string name }
                
                @count(2)
                resource vm main {
                    name = "main-${count}"
                }
                
                @count(2)
                resource vm second {
                    name = main.name
                }
                """);
        var main0 = interpreter.getInstance("main[0]");
        var main1 = interpreter.getInstance("main[1]");
        Assertions.assertNotNull(main0);
        Assertions.assertNotNull(main1);
        Assertions.assertEquals("main-0", main0.get("name"));
        Assertions.assertEquals("main-1", main1.get("name"));
        var second0 = interpreter.getInstance("second[0]");
        var second1 = interpreter.getInstance("second[1]");
        Assertions.assertNotNull(second0);
        Assertions.assertNotNull(second1);
        Assertions.assertEquals("main-0", second0.get("name"));
        Assertions.assertEquals("main-1", second1.get("name"));
    }

    @Test
    void countResourceDependencyIndex() {
        eval("""
                schema vm { string name }
                
                resource vm main {
                    name = "main-name"
                }
                
                @count(2)
                resource vm second {
                    name = main.name
                }
                """);
        var main0 = interpreter.getInstance("main");
        Assertions.assertNotNull(main0);
        Assertions.assertEquals("main-name", main0.get("name"));
        var second0 = interpreter.getInstance("second[0]");
        var second1 = interpreter.getInstance("second[1]");
        Assertions.assertNotNull(second0);
        Assertions.assertNotNull(second1);
        Assertions.assertEquals("main-name", second0.get("name"));
        Assertions.assertEquals("main-name", second1.get("name"));
    }

    @Test
    void countComponent() {
        eval("""
                component vm { }

                @count(2)
                component vm main {

                }
                """);
    }

    @Test
    @DisplayName("@count with length() of string creates correct number of resources")
    void countWithLengthOfString() {
        eval("""
                schema vm { string id }

                var name = "ab"

                @count(length(name))
                resource vm server {
                    id = "server-$count"
                }
                """);
        // "ab" has length 2, so we should have 2 resources
        var server0 = interpreter.getInstance("server[0]");
        var server1 = interpreter.getInstance("server[1]");
        Assertions.assertNotNull(server0, "server[0] should exist");
        Assertions.assertNotNull(server1, "server[1] should exist");
        Assertions.assertEquals("server-0", server0.get("id"));
        Assertions.assertEquals("server-1", server1.get("id"));
    }

    @Test
    @DisplayName("@count with length() of array creates correct number of resources")
    void countWithLengthOfArray() {
        eval("""
                schema vm { string id }

                var zones = ["us-east-1a", "us-east-1b", "us-east-1c"]

                @count(length(zones))
                resource vm server {
                    id = "server-$count"
                }
                """);
        // zones has 3 elements, so we should have 3 resources
        Assertions.assertNotNull(interpreter.getInstance("server[0]"));
        Assertions.assertNotNull(interpreter.getInstance("server[1]"));
        Assertions.assertNotNull(interpreter.getInstance("server[2]"));
        Assertions.assertEquals("server-0", interpreter.getInstance("server[0]").get("id"));
        Assertions.assertEquals("server-1", interpreter.getInstance("server[1]").get("id"));
        Assertions.assertEquals("server-2", interpreter.getInstance("server[2]").get("id"));
    }

    @Test
    @DisplayName("@count with length() of var assigned from resource property creates correct number of resources")
    void countWithLengthOfVarFromResourceProperty() {
        // When the resource property is assigned to a var first, length() works correctly
        eval("""
                schema network { string name }
                schema vm { string id }

                resource network subnet {
                    name = "hi"
                }

                var subnetName = subnet.name

                @count(length(subnetName))
                resource vm server {
                    id = "server-$count"
                }
                """);
        // "hi" has length 2, so we should have 2 resources
        var server0 = interpreter.getInstance("server[0]");
        var server1 = interpreter.getInstance("server[1]");
        Assertions.assertNotNull(server0, "server[0] should exist");
        Assertions.assertNotNull(server1, "server[1] should exist");
        Assertions.assertEquals("server-0", server0.get("id"));
        Assertions.assertEquals("server-1", server1.get("id"));
    }

}
