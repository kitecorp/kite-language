package cloud.kitelang.execution;

import cloud.kitelang.execution.environment.Environment;
import cloud.kitelang.execution.exceptions.NotFoundException;
import cloud.kitelang.syntax.ast.statements.ExpressionStatement;
import cloud.kitelang.syntax.literals.Identifier;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
public class EnvironmentTest {
    private Environment environment;

    @BeforeEach
    void init() {
        environment = new Environment();
    }

    @Test
    void declareVar() {
        var res = environment.init("x", 1);
        var expected = 1;
        Assertions.assertEquals(expected, res);
        Assertions.assertEquals(expected, environment.get("x"));
    }

    @Test
    void lookupVar() {
        Assertions.assertThrows(NotFoundException.class, () -> environment.lookup("y"));
    }

    @Test
    void lookupGlobalVar() {
        var global = new Environment();
        global.init("VERSION", 10);
        var interpreter = new Interpreter(global);
        Assertions.assertEquals(10, interpreter.visit(ExpressionStatement.expressionStatement(Identifier.id("VERSION"))));
    }

}
