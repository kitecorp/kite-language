package io.kite.Integration;

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
                            var x = "Hello ${vm.main.name}! Your number is ${vm.main.size}"
                            println(x)
                        }
                        main()
                """);
    }

    @Test
    void interpolationOfCount() {
        var x = eval("""
                    schema vm { string name; number size; }
                
                    @count(2)
                    resource vm main {
                        name = "main-property-${count}"
                        size = 1
                    }
                
                    var x = vm.main[0].name
                """);
        System.out.println(x);
    }
}
