package io.kite.Runtime;

import io.kite.Base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Log4j2
public class UnionTypeTest extends RuntimeTest {


    @Test
    void typeNum() {
        var res = eval("type num = 1");
        Assertions.assertTrue(global.hasVar("num"));
        assertEquals(List.of(1), res);
        log.info(res);
    }

    @Test
    void typeBoolTrue() {
        var res = eval("type bool = true");
        Assertions.assertTrue(global.hasVar("bool"));
        assertEquals(List.of(true), res);
        log.info(res);
    }

    @Test
    void typeBoolFalse() {
        var res = eval("type bool = false");
        Assertions.assertTrue(global.hasVar("bool"));
        assertEquals(List.of(false), res);
        log.info(res);
    }

    @Test
    void typeString() {
        var res = eval("type x = \"hello\"");
        Assertions.assertTrue(global.hasVar("x"));
        assertEquals(List.of("hello"), res);
        log.info(res);
    }

    @Test
    void typeUnionString() {
        var res = eval("""
                type x = "hello" | "world"
                """);
        Assertions.assertTrue(global.hasVar("x"));
        assertEquals(List.of("hello", "world"), res);
        assertEquals(List.of("hello", "world"), global.get("x"));
        log.info(res);
    }

    @Test
    void typeUnionNum() {
        var res = eval("type num = 1 | 2 | 5");
        Assertions.assertTrue(global.hasVar("num"));
        assertNull(global.get("x"));
        assertEquals(List.of(1, 2, 5), res);
        log.info(res);
    }

    @Test
    void typeUnionBool() {
        var res = eval("type num = true | false");
        Assertions.assertTrue(global.hasVar("num"));
        assertNull(global.get("x"));
        assertEquals(List.of(true, false), res);
        log.info(res);
    }


}
