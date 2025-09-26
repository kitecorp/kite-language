package io.kite.TypeChecker.Decorators;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.TypeError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TypeChecker @dependsOn")
public class DependsOnTest extends CheckerTest {

    @Test
    void dependsOnResource() {
        var res = eval("""
                schema vm {}
                
                resource vm first { }
                
                @dependsOn(vm.first)
                resource vm something {}
                
                """);

    }

    @Test
    void dependsOnString() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                resource vm first { }
                
                @dependsOn("vm.first")
                resource vm something {}
                
                """)
        );
    }

    @Test
    void dependsOnNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                resource vm first { }
                
                @dependsOn(10)
                resource vm something {}
                
                """)
        );
    }

    @Test
    void dependsOnObject() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                
                resource vm first { }
                
                @dependsOn(true)
                resource vm something {}
                """)
        );
    }


}
