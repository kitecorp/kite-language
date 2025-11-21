package io.kite.typechecker;

import io.kite.base.CheckerTest;
import io.kite.typechecker.types.ObjectType;
import io.kite.typechecker.types.SchemaType;
import io.kite.typechecker.types.ValueType;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("TypeChecker Schema")
public class SchemaTest extends CheckerTest {

    @Test
    void equals() {
        var typeEnv = new TypeEnvironment(Map.of("VERSION", ValueType.String));
        var base = new SchemaType("dog", typeEnv);
        var schema = new SchemaType("cat", typeEnv);
        var schema2 = new SchemaType("cat", typeEnv);

        assertEquals(schema, schema2);
    }

    @Test
    void empty() {
        var actual = checker.visit(parse("""
                schema Vm {
                
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());
        assertEquals(new SchemaType("Vm", checker.getEnv()), actual);
    }

    @Test
    void singleProperty() {
        var actual = checker.visit(parse("""
                schema Vm {
                   number   x  
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        SchemaType vm = new SchemaType("Vm", checker.getEnv());
        vm.setProperty("x", ValueType.Number);
        assertEquals(vm, actual);
    }

    @Test
    void singlePropertyObject() {
        var actual = checker.visit(parse("""
                schema Vm {
                   object   x  
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        SchemaType vm = new SchemaType("Vm", checker.getEnv());
        vm.setProperty("x", ObjectType.INSTANCE);
        assertEquals(vm, actual);
    }


    @Test
    @DisplayName("single property object with properties checks if type was correctly parsed")
    void singlePropertyObjectWrontAssignment() {
        Assertions.assertThrows(TypeError.class, () -> {
            checker.visit(parse("""
                    schema Vm {
                       object   x  = false
                    }
                    """));
        });
    }

    @Test
    void singlePropertyInit() {
        var actual = checker.visit(parse("""
                schema Vm {
                   number    x = 1
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        var vm = new SchemaType("Vm", checker.getEnv());
        vm.setProperty("x", ValueType.Number);
        assertEquals(vm, actual);
    }

    @Test
    void objectInit() {
        var actual = checker.visit(parse("""
                schema Vm {
                   object x = {
                     size: 1
                   }
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        var vm = new SchemaType("Vm", checker.getEnv());
        var typeEnv = new TypeEnvironment(Map.of("size", ValueType.Number));
        vm.setProperty("x", new ObjectType(typeEnv));
        assertEquals(vm, actual);
    }

    @Test
    void singlePropertyInitThrows() {
        Assertions.assertThrows(TypeError.class, () -> checker.visit(parse("""
                schema Vm {
                   number  x   = true
                }
                """)));
    }

    @Test
    void multipleProperty() {
        var actual = checker.visit(parse("""
                schema Vm {
                   number x  
                   string y  
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        var vm = new SchemaType("Vm", checker.getEnv());
        vm.setProperty("x", ValueType.Number);
        vm.setProperty("y", ValueType.String);
        assertEquals(vm, actual);
    }

    @Test
    void multiplePropertyInit() {
        var actual = checker.visit(parse("""
                schema Vm {
                   number x   = 2
                   string y   = "test"
                }
                """));
        assertEquals(SchemaType.class, actual.getClass());

        var vm = new SchemaType("Vm", checker.getEnv());
        vm.setProperty("x", ValueType.Number);
        vm.setProperty("y", ValueType.String);
        assertEquals(vm, actual);
    }


}
