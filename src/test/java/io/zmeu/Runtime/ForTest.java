package io.zmeu.Runtime;

import io.zmeu.Base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

@Log4j2
public class ForTest extends RuntimeTest {

    @Test
    void increment() {
        var res = eval("""
                 var a = [for index in 1..5: 'item-$index']
                """);
        log.warn(res);
    }


}
