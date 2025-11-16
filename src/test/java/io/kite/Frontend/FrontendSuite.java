package io.kite.Frontend;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "io.kite.Frontend.Parse",
        "io.kite.Frontend.Token",
        "io.kite.Frontend.Parser",
        "io.kite.Frontend.Lexical"
})
public class FrontendSuite {
}
