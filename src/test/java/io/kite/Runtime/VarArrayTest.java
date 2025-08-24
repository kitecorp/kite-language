package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class VarArrayTest extends RuntimeTest {

    @Test
    void testVarEmptyArray() {
        var res = eval("""
                var x = []
                """);
        Assertions.assertTrue(global.hasVar("x"));
        var x = global.lookup("x");
        Assertions.assertInstanceOf(List.class, x);
    }

    @Test
    void testNumber() {
        eval("""
                var x = [1, 2]
                """);
        var varType = (List) global.lookup("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of(1, 2), varType);
    }

    @Test
    void testDecimal() {
        eval("""
                var x = [1.1, 2.2]
                """);
        var varType = (List) global.lookup("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of(1.1, 2.2), varType);
    }

    @Test
    void testBoolean() {
        eval("""
                var x = [true,false]
                """);
        var varType = (List) global.lookup("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of(true, false), varType);
    }


    @Test
    void testObject() {
        eval("""
                var x = [{env: "prod"}, {env: "dev"}]
                """);
        var varType = (List) global.lookup("x");
        Assertions.assertNotNull(varType);
        assertEquals(List.of(Map.of("env", "prod"), Map.of("env", "dev")), varType);
    }


}
