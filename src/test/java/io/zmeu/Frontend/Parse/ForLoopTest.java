package io.zmeu.Frontend.Parse;

import io.zmeu.Frontend.Parse.Literals.NumberLiteral;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Frontend.Parser.Statements.BlockExpression;
import io.zmeu.Frontend.Parser.Statements.ForStatement;
import io.zmeu.Frontend.Parser.Statements.WhileStatement;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parse.Literals.Identifier.id;
import static io.zmeu.Frontend.Parse.Literals.NumberLiteral.number;
import static io.zmeu.Frontend.Parse.Literals.ObjectLiteral.object;
import static io.zmeu.Frontend.Parse.Literals.StringLiteral.string;
import static io.zmeu.Frontend.Parser.Expressions.ArrayExpression.array;
import static io.zmeu.Frontend.Parser.Expressions.AssignmentExpression.assign;
import static io.zmeu.Frontend.Parser.Expressions.BinaryExpression.binary;
import static io.zmeu.Frontend.Parser.Expressions.ObjectExpression.objectExpression;
import static io.zmeu.Frontend.Parser.Expressions.ResourceExpression.resource;
import static io.zmeu.Frontend.Parser.Expressions.VarDeclaration.var;
import static io.zmeu.Frontend.Parser.Statements.BlockExpression.block;
import static io.zmeu.Frontend.Parser.Statements.ExpressionStatement.expressionStatement;
import static io.zmeu.Frontend.Parser.Statements.IfStatement.If;
import static io.zmeu.Frontend.Parser.Statements.VarStatement.varStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class ForLoopTest extends ParserTest {

    @Test
    void test() {
        var res = parse("""
                while (x>10) { 
                    x+=1
                }
                """);
        var expected = Program.of(
                WhileStatement.builder()
                        .test(binary(id("x"), NumberLiteral.of(10), ">"))
                        .body(expressionStatement(block(expressionStatement(assign("+=", id("x"), NumberLiteral.of(1))
                                        )
                                )
                        )).build()

        );
        log.info((res));
        assertEquals(expected, res);
    }

    @Test
    void testForInRange() {
        var res = parse("""
                for i in 0..10 {
                    i+=1
                }
                """);
        var expected = Program.of(
                ForStatement.builder()
                        .item(id("i"))
                        .range(Range.of(0, 10))
                        .body(expressionStatement(block(
                                expressionStatement(
                                        assign("+=", id("i"), number(1))
                                )
                        )))
                        .build()
        );
        log.info(res);
        assertEquals(expected, res);
    }

    @Test
    void testFor() {
        var res = parse("""
                [for i in 0..10: i+=1]
                """);
        var expected = Program.of(
                expressionStatement(array(
                        ForStatement.builder()
                                .item(id("i"))
                                .range(Range.of(0, 10))
                                .body(expressionStatement(
                                        assign("+=", id("i"), number(1))
                                ))
                                .build()
                ))
        );
        log.info(res);
        assertEquals(expected, res);
    }

    @Test
    void testForConditional() {
        var res = parse("""
                [for i in 0..10: if i>2 i+=1]
                """);
        var expected = Program.of(
                expressionStatement(array(
                        ForStatement.builder()
                                .item(id("i"))
                                .range(Range.of(0, 10))
                                .body(
                                        If(binary(">", "i", 2),
                                                expressionStatement(assign("+=", id("i"), number(1)))
                                        )
                                )
                                .build()
                ))
        );
        log.info(res);
        assertEquals(expected, res);
    }

    @Test
    void arrayAssignedToVar() {
        var res = parse("""
                var x = [for index in 1..5: 'item-$index']
                """);
        var expected = Program.of(
                varStatement(var("x",
                        array(
                                ForStatement.builder()
                                        .item(id("index"))
                                        .range(Range.of(1, 5))
                                        .body(expressionStatement(string("item-$index")))
                                        .build()
                        )))
        );
        log.info(res);
        assertEquals(expected, res);
    }

    @Test
    void arrayObjectsAssignedToVar() {
        var res = parse("""
                var x = [for index in 1..5: { name: 'item-$index'}]
                """);
        var expected = Program.of(
                varStatement(var("x",
                        array(
                                ForStatement.builder()
                                        .item(id("index"))
                                        .range(Range.of(1, 5))
                                        .body(expressionStatement(objectExpression(object("name", string("item-$index")))))
                                        .build()
                        )))
        );
        log.info(res);
        assertEquals(expected, res);
    }

    @Test
    void arrayResourcesAssignedToVar() {
        var res = parse("""
                var envs = [{client: 'amazon'},{client: 'bmw'}]
                [for index in envs]
                resource Bucket photos {
                  name     = 'name-${index.value}'
                }
                """);
        var expected = Program.of(
                varStatement(var("envs", array(objectExpression(object("client", string("amazon"))), objectExpression(object("client", string("bmw")))))),
                expressionStatement(array(ForStatement.builder()
                        .item(id("index"))
                        .body(
                                resource("Bucket", "photos",
                                        (BlockExpression) block(assign("name", "'name-${index.value}'")))
                        )
                        .build()))
        );
        log.info(res);
        assertEquals(expected, res);
    }


}
