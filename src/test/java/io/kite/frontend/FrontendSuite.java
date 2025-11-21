package io.kite.frontend;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "io.kite.frontend.parse",
        "io.kite.frontend.token",
        "io.kite.frontend.parser",
        "io.kite.frontend.lexical"
})
public class FrontendSuite {
}
