package io.kite;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
        "io.kite.Frontend.Parse",
        "io.kite.TypeChecker",
        "io.kite.Runtime",
        "io.kite.Frontend.Token",
        "io.kite.Frontend.Parser",
        "io.kite.Frontend.Lexical"
})
public class _Suite {
}
