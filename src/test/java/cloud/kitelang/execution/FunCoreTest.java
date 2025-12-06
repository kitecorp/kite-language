package cloud.kitelang.execution;

import cloud.kitelang.base.RuntimeTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@Disabled
public class FunCoreTest extends RuntimeTest {

    @Test
    void funDeclaration() {
        var res = (String) eval("""
                date()
                """);

        assertEquals(LocalDate.now().toString(), res);
    }


}
