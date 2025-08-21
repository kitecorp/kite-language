package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        // 1+1+2=4
        assertEquals(4, res);
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
        assertEquals("test 012", res);
    }

    @Test
    @DisplayName("Check variable i is local to the for loop by declaring multiple loops")
    void testForInit() {
        var res = eval("""
                var x = "test "
                for i in 0..3 {
                    x += i
                }
                for i in 0..3 {
                    x += i
                }
                """);
        assertEquals("test 012012", res);
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
    @DisplayName("Inline string array")
    void testInlineStringArray() {
        var res = eval("""
                var res = []
                for env in ["dev","prod"] {
                    res += env
                }
                """);
        assertEquals(List.of("dev", "prod"), res);
        assertEquals(List.of("dev", "prod"), interpreter.getEnv().lookup("res"));
    }

    @Test
    @DisplayName("Inline numbers array")
    void testInlineNumbersArray() {
        var res = eval("""
                var res = []
                for env in [1,2,3,4,5] {
                    res += env
                }
                """);
        assertEquals(List.of(1, 2, 3, 4, 5), res);
        assertEquals(List.of(1, 2, 3, 4, 5), interpreter.getEnv().lookup("res"));
    }

    @Test
    @DisplayName("Inline objects array")
    void testInlineObjectsArray() {
        var res = eval("""
                var res = []
                for env in [{client: "dev"}, {client: "prod"}] {
                    res += env
                }
                """);
        assertEquals(List.of(Map.of("client", "dev"), Map.of("client", "prod")), res);
        assertEquals(List.of(Map.of("client", "dev"), Map.of("client", "prod")), interpreter.getEnv().lookup("res"));
    }

    @Test
    @DisplayName("Access object values in loop")
    void testAccessObjectValuesInLoop() {
        var res = eval("""
                var res = []
                for env in [{client: "dev"}, {client: "prod"}] {
                    res += env.client
                }
                """);
        assertEquals(List.of("dev", "prod"), res);
        assertEquals(List.of("dev", "prod"), interpreter.getEnv().lookup("res"));
    }
    @Test
    @DisplayName("Key Value access")
    void testKeyValue() {
        var res = eval("""
                var res = []
                for index, item in [{client: "dev"}, {client: "prod"}] {
                    res += index + " " + item.client
                }
                """);
        assertEquals(List.of("0 dev", "1 prod"), res);
        assertEquals(List.of("0 dev", "1 prod"), interpreter.getEnv().lookup("res"));
    }

    @Test
    @DisplayName("Allow both index and value in for loop")
    void testBothIndexAndValue() {
        var res = eval("""
                var indices = []
                var values = []
                for index, value in [1,2,3] {
                    indices += index
                    values += value
                }
                """);

        assertEquals(List.of(0, 1, 2), interpreter.getEnv().lookup("indices"));
        assertEquals(List.of(1, 2, 3), interpreter.getEnv().lookup("values"));
    }

    @Test
    @DisplayName("Allow both index and value in for loop string")
    void testBothIndexAndValueString() {
        var res = eval("""
                var indices = []
                var values = []
                for index, value in ["env1","env2"] {
                    indices += index
                    values += value
                }
                """);

        assertEquals(List.of(0, 1), interpreter.getEnv().lookup("indices"));
        assertEquals(List.of("env1", "env2"), interpreter.getEnv().lookup("values"));
    }

    @Test
    @DisplayName("Allow both index and value in for loop object")
    void testBothIndexAndValueObject() {
        var res = eval("""
                var indices = []
                var values = []
                for index, value in [{client: "dev"}, {client: "prod"}] {
                    indices += index
                    values += value.client
                }
                """);
        assertEquals(List.of(0, 1), interpreter.getEnv().lookup("indices"));
        assertEquals(List.of("dev", "prod"), interpreter.getEnv().lookup("values"));
    }


    @Test
    void testFor() {
        var res = eval("""
                [for i in 0..3: i+=1]
                """);

        assertEquals(List.of(1, 2, 3), res);
    }

    @Test
    void testForConditional() {
        var res = eval("""
                [for i in 0..10: if i>2 i]
                """);
        assertEquals(List.of(3, 4, 5, 6, 7, 8, 9), res);
        log.info(res);
    }

    @DisplayName("Check variable i is local to the for loop by declaring multiple loops")
    @Test
    void testMultipleLoopsDontConflict() {
        var res = eval("""
                [for i in 0..10: if i>2 i]
                [for i in 0..10: if i>3 i]
                """);
        assertEquals(List.of(4, 5, 6, 7, 8, 9), res);
        log.info(res);
    }

    @Test
    void testForConditionalElse() {
        var res = eval("""
                [for i in 0..10: if i>2 i else i+10]
                """);
        assertEquals(List.of(10, 11, 12, 3, 4, 5, 6, 7, 8, 9), res);
    }

    @Test
    void testForStringDoubleQuotes() {
        var res = eval("""
                [for i in 0..1: "item-$i"]
                """);

        assertEquals(List.of("item-0"), res);
    }

    @Test
    void arrayAssignedToVar() {
        var res = eval("""
                var x = [for index in 0..5: 'item-$index']
                """);
        assertEquals(List.of("item-0", "item-1", "item-2", "item-3", "item-4"), res);
        log.info(res);
    }

    @Test
    void arrayObjectsAssignedToVar() {
        var res = eval("""
                var x = [for index in 0..2: { name: 'item-$index'}]
                """);
        assertEquals(List.of(Map.of("name", "item-0"), Map.of("name", "item-1")), res);
    }

}
