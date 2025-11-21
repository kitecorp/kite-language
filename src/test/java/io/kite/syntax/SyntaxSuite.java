package io.kite.syntax;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "io.kite.syntax.parser",
        "io.kite.syntax.token",
        "io.kite.syntax.ast"
})
public class SyntaxSuite {
}
