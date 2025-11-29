package cloud.kitelang;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "cloud.kitelang.syntax.parser",
        "cloud.kitelang.semantics",
        "cloud.kitelang.execution",
        "cloud.kitelang.integration",
        "cloud.kitelang.syntax.token",
        "cloud.kitelang.syntax.ast",
        "cloud.kitelang.semantics.scope",
        "cloud.kitelang.stdlib"
})
public class AllTestsSuite {
}
