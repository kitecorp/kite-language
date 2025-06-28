package io.zmeu.Runtime;

import io.zmeu.Base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class ForTest extends RuntimeTest {

    @Test
    void increment() {
        var res = eval("""
                 var a = 0
                 var temp
                 for (var b = 1; a < 100; b = temp + b) {
                   temp = a;
                   a = b;
                 }
                """);
        log.warn((res));
        assertEquals(233, res);
    }

    @Test
    void incrementEq() {
        var res = eval("""
                 for (var a = 1; a < 10; a=a+1) {
                   a;
                 }
                """);
        log.warn((res));
        assertEquals(10, res);
    }

    @Test
    void incrementTestOnly() {
        var res = eval("""
                var a = 1
                 for (; a < 10; a=a+1) {
                   a
                 }
                """);
        log.warn((res));
        assertEquals(10, res);
    }


}
