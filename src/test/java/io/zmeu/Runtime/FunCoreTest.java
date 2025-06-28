package io.zmeu.Runtime;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class FunCoreTest extends BaseRuntimeTest {

    @Test
    void funDeclaration() {
        var res = (String) eval("""
                date()
                """);

        log.warn(toJson(res));
        assertEquals(LocalDate.now().toString(), res);
    }


}
