package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class VarArrayTest extends RuntimeTest {

    @Test
    void testVarEmptyArray() {
        var res = eval("""
                var x = []
                """);
        Assertions.assertTrue(interpreter.hasVar("x"));
        var x = interpreter.getVar("x");
        Assertions.assertInstanceOf(List.class, x);
    }

    @Test
    void testNumber() {
        eval("""
                var x = [1, 2]
                """);
        var varType = (List) interpreter.getVar("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of(1, 2), varType);
    }

    @Test
    void testDecimal() {
        eval("""
                var x = [1.1, 2.2]
                """);
        var varType = (List) interpreter.getVar("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of(1.1, 2.2), varType);
    }

    @Test
    void testBoolean() {
        eval("""
                var x = [true,false]
                """);
        var varType = (List) interpreter.getVar("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of(true, false), varType);
    }


    @Test
    void testObject() {
        eval("""
                var x = [{env: "prod"}, {env: "dev"}]
                """);
        var varType = (List) interpreter.getVar("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of(Map.of("env", "prod"), Map.of("env", "dev")), varType);
    }

    @Test
    void testDeclareTypeNumber() {
        eval("""
                var number[] x = []
                """);
        Assertions.assertTrue(interpreter.hasVar("x"));
        var x = interpreter.getVar("x");
        Assertions.assertInstanceOf(List.class, x);
    }

    @Test
    void testDeclareTypeString() {
        eval("""
                var string[] x = []
                """);
        Assertions.assertTrue(interpreter.hasVar("x"));
        var x = interpreter.getVar("x");
        Assertions.assertInstanceOf(List.class, x);
    }

    @Test
    void testDeclareTypeBoolean() {
        eval("""
                var boolean[] x = []
                """);
        Assertions.assertTrue(interpreter.hasVar("x"));
        var x = interpreter.getVar("x");
        Assertions.assertInstanceOf(List.class, x);
    }

    @Test
    void testDeclareTypeAny() {
        eval("""
                var any[] x = []
                """);
        Assertions.assertTrue(interpreter.hasVar("x"));
        var x = interpreter.getVar("x");
        Assertions.assertInstanceOf(List.class, x);
    }

    @Test
    void testDeclareTypeNumberInit() {
        eval("""
                var number[] x = [1,2,3]
                """);
        var varType = (List) interpreter.getVar("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of(1, 2, 3), varType);
    }

    @Test
    void testDeclareTypeStringInit() {
        eval("""
                var string[] x = ["hi",'hello']
                """);
        var varType = (List) interpreter.getVar("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of("hi", "hello"), varType);
    }

    @Test
    void testAnyString() {
        eval("""
                var any[] x = ["hi",'hello']
                """);
        var varType = (List) interpreter.getVar("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of("hi", "hello"), varType);
    }

    @Test
    void testAnyNumbers() {
        eval("""
                var any[] x = [1,2,3]
                """);
        var varType = (List) interpreter.getVar("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of(1, 2, 3), varType);
    }

    @Test
    void testAnyBoolean() {
        eval("""
                var any[] x = [true]
                """);
        var varType = (List) interpreter.getVar("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of(true), varType);
    }

    @Test
    void testAnyNull() {
        eval("""
                var any[] x = ['str', null]
                """);
        var varType = (List) interpreter.getVar("x");
        Assertions.assertNotNull(varType);
        ArrayList<Object> expected = new ArrayList<>();
        expected.add("str");
        expected.add(null);
        assertEquals(expected, varType);
    }

    @Test
    void testAnyObject() {
        eval("""
                var any[] x = [{env: "prod"}, {env: "dev"}]
                """);
        var varType = (List) interpreter.getVar("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of(Map.of("env", "prod"), Map.of("env", "dev")), varType);
    }

    @Test
    void testAnyMix() {
        eval("""
                var any[] x = [{env: "prod"}, {env: "dev"}, 'hello', 123, true]
                """);
        var varType = (List) interpreter.getVar("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of(Map.of("env", "prod"), Map.of("env", "dev"), "hello", 123, true), varType);
    }

    @Test
    void testTypeAny() {
        eval("""
                var x = ["hi", 1, true]
                """);
        var varType = (List) interpreter.getVar("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of("hi", 1, true), varType);
    }


}
