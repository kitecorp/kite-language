package io.kite.Runtime.Decorators;

import io.kite.Runtime.CycleException;
import io.kite.Runtime.Values.ResourceValue;
import io.kite.Runtime.Values.SchemaValue;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * All the tests from print but with added sensitive values.
 */
@Log4j2
public class DependsOnTests extends DecoratorTests {

    @Test
    void dependsOnSingleResource() {
        var res = (ResourceValue) eval("""
                schema vm { string name }
                
                resource vm first { }
                
                @dependsOn(vm.first)
                resource vm second {
                
                }
                """);
        Assertions.assertTrue(res.getDependencies().contains("first"));
        log.warn(res.getDependencies());
    }

    @Test
    void dependsOnMultipleResources() {
        var res = (ResourceValue) eval("""
                schema vm { string name }
                
                resource vm first { }
                resource vm main { }
                
                @dependsOn([vm.first, vm.main])
                resource vm second {
                
                }
                """);
        Assertions.assertTrue(res.getDependencies().contains("first"));
        Assertions.assertTrue(res.getDependencies().contains("main"));
        log.warn(res.getDependencies());
    }


    @Test
    void dependsOnMultipleResourcesLateinit() {
        eval("""
                schema vm { string name }
                
                resource vm first { }
                resource vm main { }
                
                @dependsOn([vm.first, vm.main, vm.third])
                resource vm second {
                
                }
                
                resource vm third { }
                """);
        var res = ((SchemaValue) interpreter.getEnv().get("vm")).findInstance("second");
        Assertions.assertTrue(res.getDependencies().contains("first"));
        Assertions.assertTrue(res.getDependencies().contains("main"));
        Assertions.assertTrue(res.getDependencies().contains("third"));
        log.warn(res.getDependencies());
    }

    @Test
    void dependsOnCycleFirstObjectSecondObject() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                @dependsOn(vm.third)
                resource vm second {
                
                }
                
                @dependsOn(vm.second)
                resource vm third { }
                """));
    }

    @Test
    void dependsOnCycleFirstObjectSecondArray() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                @dependsOn(vm.third)
                resource vm second {
                
                }
                
                @dependsOn([vm.second])
                resource vm third { }
                """));
    }

    @Test
    void dependsOnCycleFirstArraySecondArray() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                @dependsOn([vm.third])
                resource vm second {
                
                }
                
                @dependsOn([vm.second])
                resource vm third { }
                """));
    }

    @Test
    void dependsOnCycleFirstArraySecondObject() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                @dependsOn([vm.third])
                resource vm second {
                
                }
                
                @dependsOn(vm.second)
                resource vm third { }
                """));
    }

    @Test
    void dependsOnCycleFirstArraySecondObjectMultiple() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                resource vm first { }
                resource vm main { }
                
                @dependsOn([vm.first, vm.main, vm.third])
                resource vm second {
                
                }
                
                @dependsOn(vm.second)
                resource vm third { }
                """));
    }

    @Test
    void dependsOnCycleFirstArraySecondArrayMultiple() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                resource vm first { }
                resource vm main { }
                
                @dependsOn([vm.first, vm.main, vm.third])
                resource vm second {
                
                }
                
                @dependsOn([vm.second, vm.main])
                resource vm third { }
                """));
    }

    @Test
    void dependsOnCycleFirstArraySecondArrayMultipleLateResources() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                
                
                @dependsOn([vm.first, vm.main, vm.third])
                resource vm second {
                
                }
                
                @dependsOn([vm.second, vm.main])
                resource vm third { }
                
                resource vm first { }
                resource vm main { }
                """));
    }

    @Test
    void dependsOnCycleSingleArray() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                @dependsOn([vm.third])
                resource vm second {
                
                }
                
                @dependsOn([vm.second])
                resource vm third { }
                """));
    }

}
