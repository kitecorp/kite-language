package cloud.kitelang.base;

import cloud.kitelang.analysis.ImportResolver;
import cloud.kitelang.semantics.TypeChecker;
import cloud.kitelang.semantics.types.Type;
import cloud.kitelang.syntax.ast.Program;
import cloud.kitelang.syntax.parser.ParserTest;
import cloud.kitelang.tool.theme.PlainTheme;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.nio.file.Path;

public class CheckerTest extends ParserTest {
    /**
     * Base path for resolving relative import paths in tests.
     * Points to src/test/resources so tests can use clean paths like "providers/networking".
     */
    protected static final Path TEST_RESOURCES_PATH = Path.of("src/test/resources");

    protected TypeChecker checker;
    protected Program program;

    @BeforeEach
    void setUp() {
        checker = new TypeChecker();
        checker.getPrinter().setTheme(new PlainTheme());

        // Set base path for import resolution so tests can use relative paths
        ImportResolver.setBasePath(TEST_RESOURCES_PATH);
    }

    @AfterEach
    void cleanup() {
        program = null;
        checker = null;
        // Clear base path after each test
        ImportResolver.setBasePath(null);
    }

    @Override
    protected Type eval(String source) {
        program = super.parse(source);
        return checker.visit(program);
    }

}
