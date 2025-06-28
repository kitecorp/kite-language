package io.zmeu.Frontend.Parse;

import io.zmeu.Runtime.BaseRuntimeTest;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.BeforeAll;


public class BaseTest extends BaseRuntimeTest {

    @BeforeAll
    static void setLog4j() {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);
    }


}
