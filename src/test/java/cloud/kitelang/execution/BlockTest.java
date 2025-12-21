package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class BlockTest extends RuntimeTest {
    @Test
    void evalLastStatement() {
        var res = eval("""
                var x=10
                var y=20
                x*y+30
                """);
        assertEquals(230, res);
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
    }
}
