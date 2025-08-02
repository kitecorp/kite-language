package io.zmeu.TypeChecker;

import io.zmeu.Base.CheckerTest;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Frontend.Parser.Statements.BlockExpression;
import io.zmeu.Frontend.Parser.Statements.ForStatement;
import io.zmeu.TypeChecker.Types.ArrayType;
import io.zmeu.TypeChecker.Types.ObjectType;
import io.zmeu.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.zmeu.Frontend.Parse.Literals.Identifier.id;
import static io.zmeu.Frontend.Parse.Literals.ObjectLiteral.object;
import static io.zmeu.Frontend.Parse.Literals.StringLiteral.string;
import static io.zmeu.Frontend.Parser.Expressions.ArrayExpression.array;
import static io.zmeu.Frontend.Parser.Expressions.AssignmentExpression.assign;
import static io.zmeu.Frontend.Parser.Expressions.ObjectExpression.objectExpression;
import static io.zmeu.Frontend.Parser.Expressions.ResourceExpression.resource;
import static io.zmeu.Frontend.Parser.Expressions.VarDeclaration.var;
import static io.zmeu.Frontend.Parser.Statements.BlockExpression.block;
import static io.zmeu.Frontend.Parser.Statements.ExpressionStatement.expressionStatement;
import static io.zmeu.Frontend.Parser.Statements.VarStatement.varStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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

        assertInstanceOf(ArrayType.class, res);
        var varType = (ArrayType) res;
        assertEquals(ValueType.Number, varType.getType());
    }

    @Test
    void testForConditional() {
        var res = eval("""
                [for i in 0..10: if i>2 i+=1]
                """);
        assertInstanceOf(ArrayType.class, res);
        var varType = (ArrayType) res;
        assertEquals(ValueType.Number, varType.getType());
    }

    @Test
    void testForStringDoubleQuotes() {
        var res = eval("""
                [for i in 0..10: "item-$i"]
                """);

        assertInstanceOf(ArrayType.class, res);
        var varType = (ArrayType) res;
        assertEquals(ValueType.String, varType.getType());
    }

    @Test
    void arrayAssignedToVar() {
        var res = eval("""
                var x = [for index in 1..5: 'item-$index']
                """);

        assertInstanceOf(ArrayType.class, res);
        var varType = (ArrayType) res;
        assertEquals(ValueType.String, varType.getType());
    }

    @Test
    void arrayObjectsAssignedToVar() {
        var res = eval("""
                var x = [for index in 1..5: { name: 'item-$index'}]
                """);
        assertInstanceOf(ArrayType.class, res);

        var varType = (ArrayType) res;
        var objectType = new ObjectType(new TypeEnvironment(varType.getEnvironment().getParent(), Map.of("name", ValueType.String)));
        assertEquals(objectType, varType.getType());
    }

    @Test
    void arrayResourcesAssignedToVar() {
        var res = eval("""
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
