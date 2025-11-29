package cloud.kitelang.base;

import cloud.kitelang.semantics.TypeChecker;
import cloud.kitelang.semantics.types.Type;
import cloud.kitelang.syntax.ast.Program;
import cloud.kitelang.syntax.parser.ParserTest;
import cloud.kitelang.tool.theme.PlainTheme;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class CheckerTest extends ParserTest {
    protected TypeChecker checker;
    protected Program program;

    @BeforeEach
    void setUp() {
        checker = new TypeChecker();
        checker.getPrinter().setTheme(new PlainTheme());
    }

    @AfterEach
    void cleanup() {
        program = null;
        checker = null;
    }

    @Override
    protected Type eval(String source) {
        program = super.parse(source);
        return checker.visit(program);
    }

}
