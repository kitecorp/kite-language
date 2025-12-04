package cloud.kitelang.syntax;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "cloud.kitelang.syntax.parser",
        "cloud.kitelang.syntax.token",
        "cloud.kitelang.syntax.ast"
})
public class SyntaxSuite {
}
