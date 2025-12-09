package cloud.kitelang.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Resource string interpolation")
public class ResourceStringInterpolationTest extends BaseIntegrationTest {
    @Test
    void stringInterpolationMemberAccess() {
        eval("""
                        schema vm { string name; number size; }
                
                        resource vm server {
                            name = "main-property"
                            size = 1
                        }
                
                        fun main() {
                            var x = "Hello ${server.name}! Your number is ${server.size}"
                            println(x)
                        }
                        main()
                """);
    }

    @Test
    void interpolationAfterCount() {
        eval("""
                    schema vm { string name; number size; }
                
                    @count(2)
                    resource vm main {
                        name = "property-${count}"
                        size = 1
                    }
                
                    var x = main[0].name
                    var y = main[1].name
                """);
        Assertions.assertEquals("property-0", interpreter.getInstance("main[0]").getProperty("name"));
        Assertions.assertEquals("property-1", interpreter.getInstance("main[1]").getProperty("name"));
    }

    @Test
    void interpolationBeforeCount() {
        eval("""
                    schema vm { string name; number size; }
                
                    var x = main[0].name
                    var y = main[1].name
                
                    @count(2)
                    resource vm main {
                        name = "property-${count}"
                        size = 1
                    }
                
                
                """);
        Assertions.assertEquals("property-0", interpreter.getInstance("main[0]").getProperty("name"));
        Assertions.assertEquals("property-1", interpreter.getInstance("main[1]").getProperty("name"));
    }
}
