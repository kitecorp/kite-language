package io.kite.Frontend.Parse;

import io.kite.Base.RuntimeTest;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeAll;


public class ParserTest extends RuntimeTest {

    @BeforeAll
    static void setLog4j() {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
    }


}
