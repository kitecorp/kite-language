package io.zmeu.Runtime;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class BlockTest extends BaseRuntimeTest {
    @Test
    void evalLastStatement() {
        var res = eval("""
                var x=10
                var y=20
                x*y+30
                """);
        assertEquals(230, res);
        log.warn((res));
    }
    @Test
    void nestedBlock() {
        var res = eval("""
                {
                    var x=10
                    {
                        var x = 2
                    }
                    x
                }
                """);
        assertEquals(10, res);
        log.warn((res));
    }

    @Test
    void nestedBlockAccess() {
        var res = eval("""
                {
                    var outer=10
                    var res = {
                        var x = 2*outer
                        x
                    }
                    res
                }
                """);
        assertEquals(20, res);
        log.warn((res));
    }

    @Test
    void nestedBlockSet() {
        var res = eval("""
                {
                    var outer=10
                     {
                        outer =  20
                    }
                    outer
                }
                """);
        assertEquals(20, res);
        log.warn((res));
    }
}
