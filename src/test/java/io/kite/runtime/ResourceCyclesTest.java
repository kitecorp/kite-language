package io.kite.runtime;

import io.kite.base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
public class ResourceCyclesTest extends RuntimeTest {


    @Test
    void checkNumberOfDependencies() {
        var res = eval("""
                schema vm {
                    string name
                    number maxCount=0
                }
                resource vm main {
                    name = "first"
                    maxCount=1
                }
                resource vm second {
                    name = "second"
                    maxCount = main.maxCount
                }
                """);
        log.warn((res));
        var resource = interpreter.getInstance("main");
        assertNotNull(resource);

        var second = interpreter.getInstance("second");
        assertNotNull(second);
        assertEquals(1, second.getDependencies().size());
    }

    @Test
    void checkAllDependenciesAreAddedToDependencyList() {
        var res = eval("""
                schema vm {
                    string name
                    number maxCount=0
                }
                resource vm main {
                    name = third.name
                    maxCount=second.maxCount
                }
                resource vm second  {
                    name = "second"
                    maxCount = 2
                }
                resource vm third  {
                    name = "third"
                    maxCount = 3
                }
                """);
        log.warn((res));
        var resource = interpreter.getInstance("main");
        assertNotNull(resource);

        var second = interpreter.getInstance("second");
        assertNotNull(second);
        assertEquals(2, resource.getDependencies().size());
    }

    @Test
    void checkAllDependenciesAreAddedToDependencyListDifferentOrder() {
        var res = eval("""
                schema vm {
                    string name
                    number maxCount=0
                }

                resource vm second  {
                    name = "second"
                    maxCount = 2
                }
                resource vm main {
                    name = third.name
                    maxCount=second.maxCount
                }
                resource vm third  {
                    name = "third"
                    maxCount = 3
                }
                """);
        log.warn((res));
        var resource = interpreter.getInstance("main");
        assertNotNull(resource);

        var second = interpreter.getInstance("second");
        assertNotNull(second);
        assertEquals(2, resource.getDependencies().size());
    }

    @Test
    void checkAllDependenciesAreAddedToDependencyListEarly() {
        var res = eval("""
                schema vm {
                    string name
                    number maxCount=0
                }
                resource vm second {
                    name = "second"
                    maxCount = 2
                }
                resource vm third {
                    name = "third"
                    maxCount = 3
                }
                resource vm main {
                    name = third.name
                    maxCount=second.maxCount
                }

                """);
        log.warn((res));
        var main = interpreter.getInstance("main");
        assertNotNull(main);
        assertEquals(2, main.argVal("maxCount"));
        assertEquals("third", main.argVal("name"));

        var second = interpreter.getInstance("second");
        assertNotNull(second);
        assertEquals(2, main.getDependencies().size());
    }

    @Test
    @DisplayName("Evaluate dependency")
    void resourceIsDefinedInSchemaDependencyFirst() {
        var res = eval("""
                schema vm {
                    string name
                    number maxCount=0
                    number minCount=0
                }
                resource vm second {
                    name = "second"
                    maxCount = main.maxCount
                    minCount = main.minCount
                }
                resource vm main {
                    name = "main"
                    maxCount = 2
                    minCount = 1
                }
                """);
        log.warn((res));
        var schema = interpreter.getSchema("vm");

        assertNotNull(schema);
        assertEquals("vm", schema.getType());

        var resource = interpreter.getInstance("main");
        assertNotNull(resource);
        assertEquals("main", resource.getName());
        assertEquals("main", resource.argVal("name"));
        assertEquals(2, resource.argVal("maxCount"));
        assertEquals(1, resource.argVal("minCount"));

        var second = interpreter.getInstance("second");
        assertNotNull(second);
        assertEquals("second", second.getName());
        assertEquals("second", second.argVal("name"));
        assertEquals(2, second.argVal("maxCount"));
        assertEquals(1, second.argVal("minCount"));
    }

    @Test
    @DisplayName("Evaluate dependency and pull value from global schema")
    void dependencyFirstMissingProperty() {
        var res = eval("""
                schema vm {
                    string name
                    number maxCount=0
                    number minCount=1
                }
                resource vm second {
                    name = "second"
                    maxCount = main.maxCount
                    minCount = main.minCount
                }
                resource vm main {
                    name = "main"
                    maxCount = 2
                }
                """);
        log.warn((res));
        var schema = interpreter.getSchema("vm");

        assertNotNull(schema);
        assertEquals("vm", schema.getType());

        var resource = interpreter.getInstance("main");
        assertNotNull(resource);
        assertEquals("main", resource.getName());
        assertEquals("main", resource.argVal("name"));
        assertEquals(2, resource.argVal("maxCount"));
        assertEquals(1, resource.argVal("minCount"));

        var second = interpreter.getInstance("second");
        assertNotNull(second);
        assertEquals("second", second.getName());
        assertEquals("second", second.argVal("name"));
        assertEquals(2, second.argVal("maxCount"));
        assertEquals(1, second.argVal("minCount"));
    }

    @Test
    @DisplayName("Evaluate multiple dependencies before evaluating main")
    void multipleDependencies() {
        var res = eval("""
                schema vm {
                    string name
                    number maxCount=0
                    number minCount=1
                }
                resource vm main {
                    name = "main"
                    maxCount = dep1.maxCount
                    minCount = dep2.minCount
                }
                resource vm dep1 {
                    name = "dep1"
                    maxCount = 2
                }
                resource vm dep2 {
                    name = "dep2"
                    minCount = 3
                }
                """);
        log.warn((res));
        var schema = interpreter.getSchema("vm");

        assertNotNull(schema);
        assertEquals("vm", schema.getType());

        var resource = interpreter.getInstance("dep2");
        assertNotNull(resource);
        assertEquals("dep2", resource.getName());
        assertEquals("dep2", resource.argVal("name"));
        assertEquals(0, resource.argVal("maxCount"));
        assertEquals(3, resource.argVal("minCount"));

        var dep1 = interpreter.getInstance("dep1");
        assertNotNull(dep1);
        assertEquals("dep1", dep1.getName());
        assertEquals("dep1", dep1.argVal("name"));
        assertEquals(2, dep1.argVal("maxCount"));
        assertEquals(1, dep1.argVal("minCount"));

        var main = interpreter.getInstance("main");
        assertNotNull(main);
        assertEquals("main", main.getName());
        assertEquals("main", main.argVal("name"));
        assertEquals(2, main.argVal("maxCount"));
        assertEquals(3, main.argVal("minCount"));
    }

    @Test
    @DisplayName("eval chain of dependencies before evaluating main")
    void chainOfDependencies() {
        var res = eval("""
                schema vm {
                    string name
                    number maxCount=0
                    number minCount=1
                }
                resource vm main {
                    name = "main"
                    maxCount = dep1.maxCount
                    minCount = dep2.minCount
                }
                resource vm dep1 {
                    name = "dep1"
                    maxCount = dep2.maxCount
                }
                resource vm dep2 {
                    name = "dep2"
                    minCount = 2
                    maxCount = 3
                }
                """);
        log.warn((res));
        var schema = interpreter.getSchema("vm");

        assertNotNull(schema);
        assertEquals("vm", schema.getType());

        var resource = interpreter.getInstance("dep2");
        assertNotNull(resource);
        assertEquals("dep2", resource.getName());
        assertEquals("dep2", resource.argVal("name"));
        assertEquals(3, resource.argVal("maxCount"));
        assertEquals(2, resource.argVal("minCount"));

        var dep1 = interpreter.getInstance("dep1");
        assertNotNull(dep1);
        assertEquals("dep1", dep1.getName());
        assertEquals("dep1", dep1.argVal("name"));
        assertEquals(3, dep1.argVal("maxCount"));
        assertEquals(1, dep1.argVal("minCount"));

        var main = interpreter.getInstance("main");
        assertNotNull(main);
        assertEquals("main", main.getName());
        assertEquals("main", main.argVal("name"));
        assertEquals(3, main.argVal("maxCount"));
        assertEquals(2, main.argVal("minCount"));
    }

    @Test
    @DisplayName("eval chain of dependencies with last dep using default schema")
    void chainOfDependenciesDefaultSchema() {
        var res = eval("""
                schema vm {
                    string name
                    number maxCount=0
                    number minCount=1
                }
                resource vm main {
                    name = "main"
                    maxCount = dep1.maxCount
                    minCount = dep2.minCount
                }
                resource vm dep1 {
                    name = "dep1"
                    maxCount = dep2.maxCount
                }
                resource vm dep2 {
                    name = "dep2"
                    minCount = 2
                }
                """);
        log.warn((res));
        var schema = interpreter.getSchema("vm");

        assertNotNull(schema);
        assertEquals("vm", schema.getType());

        var resource = interpreter.getInstance("dep2");
        assertNotNull(resource);
        assertEquals("dep2", resource.getName());
        assertEquals("dep2", resource.argVal("name"));
        assertEquals(0, resource.argVal("maxCount"));
        assertEquals(2, resource.argVal("minCount"));

        var dep1 = interpreter.getInstance("dep1");
        assertNotNull(dep1);
        assertEquals("dep1", dep1.getName());
        assertEquals("dep1", dep1.argVal("name"));
        assertEquals(0, dep1.argVal("maxCount"));
        assertEquals(1, dep1.argVal("minCount"));

        var main = interpreter.getInstance("main");
        assertNotNull(main);
        assertEquals("main", main.getName());
        assertEquals("main", main.argVal("name"));
        assertEquals(0, main.argVal("maxCount"));
        assertEquals(2, main.argVal("minCount"));
    }

    @Test
    @DisplayName("eval simple circular dependencies")
    void cycleSimpleDependencies() {
        assertThrows(CycleException.class, () -> eval("""
                schema vm {
                    string name
                    number maxCount=0
                    number minCount=1
                }

                resource vm main  {
                    name = "main"
                    maxCount = dep1.maxCount
                }
                resource vm dep1 {
                    name = "dep1"
                    maxCount = main.maxCount
                }
                """));

    }

    @Test
    @DisplayName("eval simple circular dependencies")
    void cycleDetectionSelf() {
        assertThrows(CycleException.class, () -> eval("""
                schema vm {
                    string name
                    number maxCount=0
                    number minCount=1
                }
                resource vm main {
                    name = "main"
                    maxCount = main.maxCount
                }
                """));

    }

    @Test
    @DisplayName("eval indirect circular dependencies")
    void cycleIndirectDependency() {
        assertThrows(CycleException.class, () -> eval("""
                schema vm {
                    string name
                    number maxCount=0
                    number minCount=1
                }
                resource vm a {
                    name = "a"
                    maxCount = b.maxCount
                }
                resource vm b {
                    name = "b"
                    maxCount = c.maxCount
                }
                resource vm c {
                    name = "c"
                    maxCount = a.maxCount
                }
                """));

    }



}
