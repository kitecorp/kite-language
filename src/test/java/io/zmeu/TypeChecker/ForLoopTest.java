package io.zmeu.TypeChecker;

import io.zmeu.Base.CheckerTest;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Frontend.Parser.Statements.BlockExpression;
import io.zmeu.Frontend.Parser.Statements.ForStatement;
import io.zmeu.TypeChecker.Types.ValueType;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.DisplayName;
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

@DisplayName("TypeChecker Loops")
public class ForLoopTest extends CheckerTest {

    @Test
    void testBlock() {
        var actual = eval("""
                var x = 10
                while (x!=0) {
                   x--
                }
                x
                """);
        assertEquals(ValueType.Number, actual);
    }


    @Test
    void testForInRange() {
        var res = eval("""
                for i in 0..10 {
                    i+=1
                }
                """);
        assertEquals(ValueType.Number, res);
    }

    @Test
    void testFor() {
        var res = eval("""
                [for i in 0..10: i+=1]
                """);

        assertEquals(ValueType.Number, res);
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

        assertEquals(expected, res);
    }


}
