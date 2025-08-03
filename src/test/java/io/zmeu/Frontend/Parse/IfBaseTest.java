package io.kite.Frontend.Parse;

import io.kite.ParserErrors;
import io.kite.Frontend.Lexer.TokenType;
import io.kite.Frontend.Parser.errors.ParseError;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.Frontend.Parser.Expressions.AssignmentExpression.assign;
import static io.kite.Frontend.Parser.Expressions.BinaryExpression.binary;
import static io.kite.Frontend.Parse.Literals.Identifier.id;
import static io.kite.Frontend.Parse.Literals.NumberLiteral.number;
import static io.kite.Frontend.Parser.Program.program;
import static io.kite.Frontend.Parser.Statements.BlockExpression.block;
import static io.kite.Frontend.Parser.Statements.ExpressionStatement.expressionStatement;
import static io.kite.Frontend.Parser.Statements.IfStatement.If;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser if case")
public class IfBaseTest extends ParserTest {

    @Test
    void test() {
        var res = parse("""
                if (x) { 
                    x=1
                }
                """);
        var expected = program(
                If(id("x"), expressionStatement(block(
                                        expressionStatement(assign("=", id("x"), number(1)))
                                )
                        )
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testXNoCurly() {
        var res = parse("""
                if (x) x=1
                """);
        var expected = program(
                If(id("x"),
                        expressionStatement(
                                assign("=", id("x"), number(1))
                        )
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    @Disabled("Add better error handling")
    void MissingOpenParenthesisError() {
        parse("""
                if x) x=1
                """);
        ParseError parseError = ParserErrors.getErrors().get(0);
        Assertions.assertEquals(TokenType.OpenParenthesis, parseError.getExpected());
    }

    @Test
    @Disabled("Add better error handling")
    void MissingCloseParenthesisError() {
        parse("""
                if (x x=1
                """);
        ParseError parseError = ParserErrors.getErrors().get(0);
        Assertions.assertEquals(TokenType.CloseParenthesis, parseError.getExpected());
    }

    @Test
    void testNoCurly() {
        var res = parse("""
                if (x) 
                    x=1
                """);
        var expected = program(
                If(id("x"),
                        expressionStatement(
                                assign("=", id("x"), number(1)))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testIfElseStatementBlocks() {
        var res = parse("""
                if (x) { 
                    1
                } else { 
                    2
                }
                """);
        var expected = program(
                If(id("x"),
                        block(expressionStatement(number(1))),
                        block(expressionStatement(number(2)))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testXBlock() {
        var res = parse("""
                if (x) {
                    if(y) x=1
                }
                """);
        var expected = program(
                If(id("x"),
                        expressionStatement(block(
                                If(
                                        id("y"),
                                        expressionStatement(
                                                assign("=", "x", number(1)))
                                )
                        ))
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testNoBlockY() {
        var res = parse("""
                if (x) 
                    if(y) x=1
                """);
        var expected = program(
                If(id("x"),
                        If(
                                id("y"),
                                expressionStatement(
                                        assign("=", "x", number(1)))
                        )
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testXCurlyY() {
        var res = parse("""
                if (x) 
                    if(y){ x=1 }
                """);
        var expected = program(
                If(id("x"),
                        If(
                                id("y"),
                                expressionStatement(block(
                                        expressionStatement(
                                                assign("=", "x", number(1))
                                        )
                                ))
                        )
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testNestedElse() {
        var res = parse("""
                if (x)
                 if(y) {} else { }
                """);
        var expected = program(
                If(id("x"),
                        If(
                                id("y"),
                                block(),
                                block()
                        )
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testNestedElseElse() {
        var res = parse("""
                if (x) 
                    if(y) {} else { } else {}
                """);
        var expected = program(
                If(id("x"),
                        If(
                                id("y"),
                                block(),
                                block()
                        ),
                        expressionStatement(block())
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testNestedElseElseInline() {
        var res = parse("""
                if (x) if(y) {} else { } else {}
                """);
        var expected = program(
                If(id("x"),
                        If(
                                id("y"),
                                block(),
                                block()
                        ),
                        expressionStatement(block())
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testNestedElseElseAssignInline() {
        var res = parse("""
                if (x) if(y) {} else { } else { x=2}
                """);
        var expected = program(
                If(id("x"),
                        If(
                                id("y"),
                                block(),
                                block()
                        ),
                        expressionStatement(block(expressionStatement(
                                assign("=", id("x"), number(2))
                        )))
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testRelationalGt() {
        var res = parse("""
                if (x > 1) {
                    x = 2;
                } else {
                    x += 2
                }
                """);
        var expected = program(
                If(
                        binary("x", 1, ">"),
                        block(expressionStatement(
                                assign("=", id("x"), number(2)))
                        ),
                        block(expressionStatement(
                                assign("+=", id("x"), number(2)))
                        )));
        log.info((res));
        assertEquals(expected, res);
    }

    @Test
    void testRelationalGtEq() {
        var res = parse("""
                if (x >= 1) {
                    x = 2;
                } else {
                    x += 2
                }
                """);
        var expected = program(
                If(
                        binary(id("x"), number(1), ">="),
                        block(expressionStatement(
                                assign("=", id("x"), number(2)))
                        ),
                        block(expressionStatement(
                                assign("+=", id("x"), number(2)))
                        )));
        log.info((res));
        assertEquals(expected, res);
    }

    @Test
    void testRelationalLt() {
        var res = parse("""
                if (x < 1) {
                    x = 2;
                } else {
                    x += 2
                }
                """);
        var expected = program(
                If(
                        binary(id("x"), number(1), "<"),
                        block(expressionStatement(
                                assign("=", id("x"), number(2)))
                        ),
                        block(expressionStatement(
                                assign("+=", id("x"), number(2)))
                        )));
        log.info((res));
        assertEquals(expected, res);
    }

    @Test
    void testRelationalLtEq() {
        var res = parse("""
                if (x <= 1) {
                    x = 2;
                } else {
                    x += 2
                }
                """);
        var expected = program(
                If(
                        binary(id("x"), number(1), "<="),
                        block(expressionStatement(
                                assign("=", id("x"), number(2)))
                        ),
                        block(expressionStatement(
                                assign("+=", id("x"), number(2)))
                        )));
        log.info((res));
        assertEquals(expected, res);
    }


}
