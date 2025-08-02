package io.zmeu.Runtime;

import io.zmeu.Base.RuntimeTest;
import io.zmeu.TypeChecker.TypeEnvironment;
import io.zmeu.TypeChecker.Types.ArrayType;
import io.zmeu.TypeChecker.Types.ObjectType;
import io.zmeu.TypeChecker.Types.ValueType;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@Log4j2
public class ForTest extends RuntimeTest {

    @Test
    void increment() {
        var res = eval("""
                 var a = [for index in 1..5: 'item-$index']
                """);
        log.warn((res));
    }

    @Test
    @DisplayName("When using block, a result is returned of the last expression")
    void testForInRange() {
        var res = eval("""
                var x = 1
                for i in 1..3 {
                    x += i
                }
                """);
        // 1+1+2+3=7
        assertEquals(7, res);
    }

    @Test
    @DisplayName("When using block, a result is returned of the last expression")
    void testForInRangeString() {
        var res = eval("""
                var x = "test "
                for i in 0..3 {
                    x += i
                }
                """);
        assertEquals("test 0123", res);
    }

    @Test
    @DisplayName("Adding elements to array works in with += operator")
    void testForInListString() {
        var res = eval("""
                var envs = ["dev","prod"]
                var res = []
                for env in envs {
                    res += env
                }
                """);
        assertEquals(List.of("dev", "prod"), res);
        assertEquals(List.of("dev", "prod"), interpreter.getEnv().lookup("res"));
    }

    @Test
    void testFor() {
        var res = eval("""
                [for i in 1..3: i+=1]
                """);

        assertEquals(List.of(2, 3, 4), res);
    }

    @Test
    void testForConditional() {
        var res = eval("""
                [for i in 0..10: if i>2 i]
                """);
        assertEquals(List.of(3, 4, 5, 6, 7, 8, 9, 10), res);
    }

    @Test
    void testForStringDoubleQuotes() {
        var res = eval("""
                [for i in 0..10: "item-$i"]
                """);

        assertInstanceOf(ArrayType.class, res);
        var varType = (ArrayType) res;
        assertEquals(ValueType.String, varType.getType());
    }

    @Test
    void arrayAssignedToVar() {
        var res = eval("""
                var x = [for index in 1..5: 'item-$index']
                """);

        assertInstanceOf(ArrayType.class, res);
        var varType = (ArrayType) res;
        assertEquals(ValueType.String, varType.getType());
    }

    @Test
    void arrayObjectsAssignedToVar() {
        var res = eval("""
                var x = [for index in 1..5: { name: 'item-$index'}]
                """);
        assertInstanceOf(ArrayType.class, res);

        var varType = (ArrayType) res;
        var objectType = new ObjectType(new TypeEnvironment(varType.getEnvironment().getParent(), Map.of("name", ValueType.String)));
        assertEquals(objectType, varType.getType());
    }

}
