package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class IfStatementTest extends RuntimeTest {

    @Test
    void consequentLess() {
        var res = eval("""
                {
                    var x = 1
                    var y = 2
                    if (x < y){
                        x = y 
                    }
                    x       
                }
                """);
        assertEquals(2, res);
    }

    @Test
    void consequentLessEq() {
        var res = eval("""
                {
                    var x = 2
                    var y = 2
                    if (x <= y){
                        x = y 
                    }
                    x       
                }
                """);
        assertEquals(2, res);
    }

    @Test
    void consequentGreat() {
        var res = eval("""
                {
                    var x = 3
                    var y = 2
                    if (x > y){
                        x = y 
                    }
                    x       
                }
                """);
        assertEquals(2, res);
    }

    @Test
    void consequentGreatEq() {
        var res = eval("""
                {
                    var x = 2
                    var y = 2
                    if (x >= y){
                        x = 3 
                    }
                    x       
                }
                """);
        assertEquals(3, res);
    }

    @Test
    void alternateGreat() {
        var res = eval("""
                {
                    var x = 1
                    var y = 2
                    if (x > y){
                        x = y 
                    } else {
                        x = 3
                    }
                    x       
                }
                """);
        assertEquals(3, res);
    }

    @Test
    void alternateGreatEq() {
        var res = eval("""
                {
                    var x = 1
                    var y = 2
                    if (x >= y){
                        x = y 
                    } else {
                        x = 3
                    }
                    x       
                }
                """);
        assertEquals(3, res);
    }

    @Test
    void alternateEq() {
        var res = eval("""
                {
                    var x = 2
                    var y = 1
                    if (x <= y){
                        x = y 
                    } else {
                        x = 3
                    }
                    x       
                }
                """);
        assertEquals(3  , res);
    }

    @Test
    void alternate() {
        var res = eval("""
                {
                    var x = 2
                    var y = 1
                    if (x < y){
                        x = y 
                    } else {
                        x = 3
                    }
                    x       
                }
                """);
        assertEquals(3, res);
    }


}
