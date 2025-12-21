package cloud.kitelang.syntax.parser;

import cloud.kitelang.base.RuntimeTest;
import org.junit.jupiter.api.BeforeAll;

import java.util.logging.Level;
import java.util.logging.Logger;


public class ParserTest extends RuntimeTest {

    @BeforeAll
    static void setLogLevel() {
        Logger.getLogger("").setLevel(Level.INFO);
    }


}
