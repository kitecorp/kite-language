package io.kite.TypeChecker.Decorators;

import io.kite.Base.CheckerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TypeChecker @dependsOn")
public class DependsOnTest extends CheckerTest {

    @Test
    void decoratorCount() {
        var res = eval("""
                schema vm {}
                
                resource vm first { }
                
                @dependsOn(vm.first)
                resource vm something {}
                
                """);

    }


}
