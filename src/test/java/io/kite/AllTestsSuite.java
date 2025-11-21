package io.kite;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "io.kite.syntax.parser",
        "io.kite.semantics",
        "io.kite.execution",
        "io.kite.integration",
        "io.kite.syntax.token",
        "io.kite.syntax.ast",
        "io.kite.semantics.scope"
})
public class AllTestsSuite {
}
