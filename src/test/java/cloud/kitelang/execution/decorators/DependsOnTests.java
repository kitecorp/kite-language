package cloud.kitelang.execution.decorators;

import cloud.kitelang.execution.CycleException;
import cloud.kitelang.execution.exceptions.NotFoundException;
import cloud.kitelang.execution.values.ResourceValue;
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
                
                @dependsOn(first)
                resource vm second {
                
                }
                """);
        Assertions.assertTrue(res.hasDependency("first"));
        log.warn(res.getDependencies());
    }

    @Test
    void dependsOnSingleResourceInexistantResource() {
        Assertions.assertThrows(NotFoundException.class, () -> eval("""
                schema vm { string name }
                
                @dependsOn(second)
                resource vm first { }
                """));
    }

    @Test
    void dependsOnMultipleResources() {
        var res = (ResourceValue) eval("""
                schema vm { string name }
                
                resource vm first { }
                resource vm main { }
                
                @dependsOn([first, main])
                resource vm second {
                
                }
                """);
        Assertions.assertTrue(res.hasDependency("first"));
        Assertions.assertTrue(res.hasDependency("main"));
        log.warn(res.getDependencies());
    }


    @Test
    void dependsOnMultipleResourcesLateinit() {
        eval("""
                schema vm { string name }
                
                resource vm first { }
                resource vm main { }
                
                @dependsOn([first, main, third])
                resource vm second {
                
                }
                
                resource vm third { }
                """);
        var res = interpreter.getInstance("second");
        Assertions.assertTrue(res.hasDependency("first"));
        Assertions.assertTrue(res.hasDependency("main"));
        Assertions.assertTrue(res.hasDependency("third"));
        log.warn(res.getDependencies());
    }

    @Test
    void dependsOnCycleFirstObjectSecondObject() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                @dependsOn(third)
                resource vm second {
                
                }
                
                @dependsOn(second)
                resource vm third { }
                """));
    }

    @Test
    void dependsOnCycleFirstObjectSecondArray() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                @dependsOn(third)
                resource vm second {
                
                }
                
                @dependsOn([second])
                resource vm third { }
                """));
    }

    @Test
    void dependsOnCycleFirstArraySecondArray() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                @dependsOn([third])
                resource vm second {
                
                }
                
                @dependsOn([second])
                resource vm third { }
                """));
    }

    @Test
    void dependsOnCycleFirstArraySecondObject() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                @dependsOn([third])
                resource vm second {
                
                }
                
                @dependsOn(second)
                resource vm third { }
                """));
    }

    @Test
    void dependsOnCycleFirstArraySecondObjectMultiple() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                resource vm first { }
                resource vm main { }
                
                @dependsOn([first, main, third])
                resource vm second {
                
                }
                
                @dependsOn(second)
                resource vm third { }
                """));
    }

    @Test
    void dependsOnCycleFirstArraySecondArrayMultiple() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                resource vm first { }
                resource vm main { }
                
                @dependsOn([first, main, third])
                resource vm second {
                
                }
                
                @dependsOn([second, main])
                resource vm third { }
                """));
    }

    @Test
    void dependsOnCycleFirstArraySecondArrayMultipleLateResources() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                
                
                @dependsOn([first, main, third])
                resource vm second {
                
                }
                
                @dependsOn([second, main])
                resource vm third { }
                
                resource vm first { }
                resource vm main { }
                """));
    }

    @Test
    void dependsOnCycleSingleArray() {
        Assertions.assertThrows(CycleException.class, () -> eval("""
                schema vm { string name }
                
                @dependsOn([third])
                resource vm second {
                
                }
                
                @dependsOn([second])
                resource vm third { }
                """));
    }

}
