package io.kite.frontend.parse;

import io.kite.frontend.parser.ValidationException;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.frontend.parse.literals.Identifier.id;
import static io.kite.frontend.parse.literals.NumberLiteral.number;
import static io.kite.frontend.parser.Program.program;
import static io.kite.frontend.parser.expressions.AssignmentExpression.assign;
import static io.kite.frontend.parser.expressions.BinaryExpression.binary;
import static io.kite.frontend.parser.statements.BlockExpression.block;
import static io.kite.frontend.parser.statements.ExpressionStatement.expressionStatement;
import static io.kite.frontend.parser.statements.IfStatement.ifStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                ifStatement(id("x"), expressionStatement(block(
                                        expressionStatement(assign("=", id("x"), number(1)))
                                )
                        )
                )
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testXNoCurly() {
        var res = parse("""
                if (x) { x=1 }
                """);
        var expected = program(
                ifStatement(id("x"),
                        expressionStatement(
                                block(assign("=", id("x"), number(1)))
                        )
                )
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void MissingOpenParenthesisError() {
        var err = assertThrows(ValidationException.class, () ->
                parse("""
                        if x) x=1
                        """)
        );
        assertEquals("""
                Parse error at line 1:4 - unmatched ')' - use both '(' and ')' or neither
                  if x) x=1
                      ^
                """.trim(), err.getMessage());
    }

    @Test
    void MissingCloseParenthesisError() {
        var err = assertThrows(ValidationException.class, () -> parse("""
                if (x x=1
                """));
        Assertions.assertEquals("""
                Parse error at line 1:7 - unmatched '(' - use both '(' and ')' or neither
                  if (x x=1
                         ^
                """.trim(), err.getMessage());
    }

    @Test
    void testNoCurly() {
        var res = parse("""
                if (x) {
                    x=1
                }
                """);
        var expected = program(
                ifStatement(id("x"),
                        expressionStatement(
                                block(assign("=", id("x"), number(1))))));
        assertEquals(expected, res);
        log.info(res);
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
                ifStatement(id("x"),
                        block(expressionStatement(number(1))),
                        block(expressionStatement(number(2)))));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testXBlock() {
        var res = parse("""
                if (x) {
                    if(y) {x=1}
                }
                """);
        var expected = program(
                ifStatement(id("x"),
                        expressionStatement(block(
                                ifStatement(
                                        id("y"),
                                        expressionStatement(block(  // ← Add block wrapper
                                                expressionStatement(assign("=", "x", number(1)))  // ← Wrap in expressionStatement
                                        ))
                                )
                        ))
                )
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testNoBlockY() {
        var res = parse("""
                if (x) {
                    if(y) { x=1 }
                }
                """);
        var expected = program(
                ifStatement(id("x"),
                        expressionStatement(block(ifStatement(
                                id("y"),
                                expressionStatement(block(
                                        expressionStatement(assign("=", "x", number(1)))
                                ))
                        )))
                )
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testXCurlyY() {
        var res = parse("""
                if (x) {
                    if(y){ x=1 }
                }
                """);
        var expected = program(
                ifStatement(id("x"),
                        expressionStatement(block(ifStatement(
                                id("y"),
                                expressionStatement(block(
                                        expressionStatement(
                                                assign("=", "x", number(1))
                                        )
                                ))
                        )))
                )
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testNestedElse() {
        var res = parse("""
                   if (x){
                    if(y) {} else { }
                }
                """);
        var expected = program(
                ifStatement(id("x"),
                        expressionStatement(block(ifStatement(
                                        id("y"),
                                        block(),
                                        block()
                                ))
                        ))
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testNestedElseElse() {
        var res = parse("""
                if (x) {
                    if(y) {} else { } 
                } else {}
                """);
        var expected = program(
                ifStatement(
                        id("x"),
                        expressionStatement(block(
                                ifStatement(
                                        id("y"),
                                        expressionStatement(block()),
                                        expressionStatement(block())  // ← This is the inner else
                                )
                        )),
                        expressionStatement(block())  // ← This is the outer else
                )
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testNestedElseElseInline() {
        var res = parse("""
                if (x) { if(y) {} else { } } else {}
                """);
        var expected = program(
                ifStatement(id("x"),
                        expressionStatement(block(
                                ifStatement(
                                        id("y"),
                                        expressionStatement(block()),
                                        expressionStatement(block())
                                )
                        )),
                        expressionStatement(block())
                )
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void testNestedElseElseAssignInline() {
        var res = parse("""
                if (x) {if(y) {} else { }} else { x=2}
                """);
        var expected = program(
                ifStatement(id("x"),
                        expressionStatement(block(
                                ifStatement(
                                        id("y"),
                                        expressionStatement(block()),
                                        expressionStatement(block())
                                )
                        )),
                        expressionStatement(block(
                                expressionStatement(assign("=", id("x"), number(2)))
                        ))
                )
        );
        assertEquals(expected, res);
        log.info(res);
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
                ifStatement(
                        binary("x", 1, ">"),
                        block(expressionStatement(
                                assign("=", id("x"), number(2)))
                        ),
                        block(expressionStatement(
                                assign("+=", id("x"), number(2)))
                        )));
        log.info(res);
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
                ifStatement(
                        binary(id("x"), number(1), ">="),
                        block(expressionStatement(
                                assign("=", id("x"), number(2)))
                        ),
                        block(expressionStatement(
                                assign("+=", id("x"), number(2)))
                        )));
        log.info(res);
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
                ifStatement(
                        binary(id("x"), number(1), "<"),
                        block(expressionStatement(
                                assign("=", id("x"), number(2)))
                        ),
                        block(expressionStatement(
                                assign("+=", id("x"), number(2)))
                        )));
        log.info(res);
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
                ifStatement(
                        binary(id("x"), number(1), "<="),
                        block(expressionStatement(
                                assign("=", id("x"), number(2)))
                        ),
                        block(expressionStatement(
                                assign("+=", id("x"), number(2)))
                        )));
        log.info(res);
        assertEquals(expected, res);
    }


}
