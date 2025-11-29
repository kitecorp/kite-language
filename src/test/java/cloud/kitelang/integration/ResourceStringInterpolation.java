package cloud.kitelang.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceStringInterpolation extends BaseIntegrationTest {
    @Test
    void stringInterpolationMemberAccess() {
        eval("""
                        schema vm { string name; number size; }
                
                        resource vm main {
                            name = "main-property"
                            size = 1
                        }
                
                        fun main() {
                            var x = "Hello ${main.name}! Your number is ${main.size}"
                            println(x)
                        }
                        main()
                """);
    }

    @Test
    void interpolationOfCount() {
        eval("""
                    schema vm { string name; number size; }
                
                    @count(2)
                    resource vm main {
                        name = "main-property-${count}"
                        size = 1
                    }
                
                    var x = main[0].name
                    var y = main[1].name
                """);
        Assertions.assertEquals("main-property-0", interpreter.getEnv().get("x"));
        Assertions.assertEquals("main-property-1", interpreter.getEnv().get("y"));
    }
}
