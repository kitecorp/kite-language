package io.kite.Runtime.Decorators;

import io.kite.Runtime.Values.SchemaValue;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * All the tests from print but with added sensitive values.
 */
@Log4j2
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
        var vm = (SchemaValue) global.get("vm");
        var main0 = vm.getInstance("main[0]");
        var main1 = vm.getInstance("main[1]");
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
        var vm = (SchemaValue) global.get("vm");
        var main0 = vm.getInstance("main[0]");
        var main1 = vm.getInstance("main[1]");
        Assertions.assertNotNull(main0);
        Assertions.assertNotNull(main1);
        Assertions.assertEquals("main-0", main0.get("name"));
        Assertions.assertEquals("main-1", main1.get("name"));
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
