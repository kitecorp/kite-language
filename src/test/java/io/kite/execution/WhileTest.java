package io.kite.execution;

import io.kite.base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class WhileTest extends RuntimeTest {

    @Test
    void increment() {
        var res = eval("""
                {
                    var x = 1
                    while (x < 5){
                        x = x+1
                    }      
                    x 
                }
                """);
        log.warn((res));
        assertEquals(5, res);
    }

    @Test
    void incrementEq() {
        var res = eval("""
                {
                    var x = 1
                    while (x <= 5){
                        x = x+1
                    }      
                    x 
                }
                """);
        log.warn((res));
        assertEquals(6, res);
    }


}
