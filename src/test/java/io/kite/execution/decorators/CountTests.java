package io.kite.execution.decorators;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * All the tests from print but with added sensitive values.
 */
@Log4j2
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
    @Disabled("not implemented")
    void countComponent() {
        eval("""
                @count(2)
                component vm main {
                
                }
                """);
    }

}
