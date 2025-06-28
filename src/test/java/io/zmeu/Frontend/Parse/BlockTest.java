package io.zmeu.Frontend.Parse;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static io.zmeu.Frontend.Parser.Program.program;
import static io.zmeu.Frontend.Parser.Statements.BlockExpression.block;
import static io.zmeu.Frontend.Parser.Statements.ExpressionStatement.expressionStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Block")
public class BlockTest extends BaseTest {

    @Test
    void testInteger() {
        var res = parse("{ 42 }");
        var expected = program(expressionStatement(block(expressionStatement(42))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testString() {
        var res = parse("{ \"hello\" }");
        var expected = program(expressionStatement(block(expressionStatement("hello"))));
        assertEquals(expected, res);
        log.info((res));
    }
    @Test
    void testEmptyBlock() {
        var res = parse("{ }    ");
        var expected = program(expressionStatement(block(Collections.emptyList())));
        assertEquals(expected, res);
        log.info((res));
    }


    @Test
    void testNestedBlocksString() {
        var res = parse("{ { \"hello\" } }");
        var expected = program(expressionStatement(block(block(expressionStatement("hello")))));
        assertEquals(expected, res);
        log.info((res));
    }


    @Test
    void testEmptyStatement() {
        var res = parse("\n");
        var expected = program();
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testEmptyStatementInBlock() {
        var res = parse("{ \n }");
        var expected = program(expressionStatement(block(Collections.emptyList())));
        assertEquals(expected, res);
        log.info((res));
    }


}
