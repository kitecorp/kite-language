package cloud.kitelang.syntax.parser;

import cloud.kitelang.syntax.ast.Program;
import cloud.kitelang.syntax.ast.statements.ForStatement;
import cloud.kitelang.syntax.ast.statements.IfStatement;
import cloud.kitelang.syntax.ast.statements.WhileStatement;
import cloud.kitelang.syntax.literals.NumberLiteral;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.ast.expressions.ArrayExpression.array;
import static cloud.kitelang.syntax.ast.expressions.AssignmentExpression.assign;
import static cloud.kitelang.syntax.ast.expressions.MemberExpression.member;
import static cloud.kitelang.syntax.ast.expressions.BinaryExpression.binary;
import static cloud.kitelang.syntax.ast.expressions.ObjectExpression.objectExpression;
import static cloud.kitelang.syntax.ast.expressions.ResourceStatement.resource;
import static cloud.kitelang.syntax.ast.expressions.StringInterpolation.interpolation;
import static cloud.kitelang.syntax.ast.expressions.VarDeclaration.var;
import static cloud.kitelang.syntax.ast.statements.BlockExpression.block;
import static cloud.kitelang.syntax.ast.statements.ExpressionStatement.expressionStatement;
import static cloud.kitelang.syntax.ast.statements.VarStatement.varStatement;
import static cloud.kitelang.syntax.literals.Identifier.id;
import static cloud.kitelang.syntax.literals.NumberLiteral.number;
import static cloud.kitelang.syntax.literals.ObjectLiteral.object;
import static cloud.kitelang.syntax.literals.StringLiteral.string;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
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
        assertEquals(expected, res);
    }

    @Test
    void testForConditional() {
        var res = parse("""
                [for i in 0..10: if i>2 { i+=1 }]
                """);
        var expected = Program.of(
                expressionStatement(array(
                        ForStatement.builder()
                                .item(id("i"))
                                .range(Range.of(0, 10))
                                .body(
                                        IfStatement.ifStatement(binary(">", "i", 2),
                                                expressionStatement(block(
                                                        expressionStatement(assign("+=", id("i"), number(1)))
                                                ))
                                        )
                                )
                                .build()
                ))
        );
        assertEquals(expected, res);
    }

    @Test
    void arrayAssignedToVar() {
        var res = parse("""
                var x = [for index in 1..5: "item-$index"]
                """);
        var expected = Program.of(
                varStatement(var("x",
                        array(
                                ForStatement.builder()
                                        .item(id("index"))
                                        .range(Range.of(1, 5))
                                        .body(expressionStatement(interpolation("item-", id("index"))))
                                        .build()
                        )))
        );
        assertEquals(expected, res);
    }

    @Test
    void arrayObjectsAssignedToVar() {
        var res = parse("""
                var x = [for index in 1..5: { name: "item-$index"}]
                """);
        var expected = Program.of(
                varStatement(var("x",
                        array(
                                ForStatement.builder()
                                        .item(id("index"))
                                        .range(Range.of(1, 5))
                                        .body(expressionStatement(objectExpression(object("name", interpolation("item-","index")))))
                                        .build()
                        )))
        );
        assertEquals(expected, res);
    }

    @Test
    void arrayResourcesAssignedToVar() {
        var res = parse("""
                var envs = [{client: 'amazon'},{client: 'bmw'}]
                [for index in envs]
                resource Bucket photos {
                  name     = "name-${index.value}"
                }
                """);
        var expected = Program.of(
                varStatement(var("envs", array(objectExpression(object("client", string("amazon"))), objectExpression(object("client", string("bmw")))))),
                expressionStatement(array(ForStatement.builder()
                        .item(id("index"))
                        .array(id("envs"))
                        .body(
                                resource("Bucket", "photos",
                                        block(assign("name", interpolation("name-", member("index", "value")))))
                        )
                        .build()))
        );
        assertEquals(expected, res);
    }


}
