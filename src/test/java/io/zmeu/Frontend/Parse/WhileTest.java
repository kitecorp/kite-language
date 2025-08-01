package io.zmeu.Frontend.Parse;

import io.zmeu.Frontend.Parse.Literals.NumberLiteral;
import io.zmeu.Frontend.Parser.Expressions.BinaryExpression;
import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Frontend.Parser.Statements.ForStatement;
import io.zmeu.Frontend.Parser.Statements.WhileStatement;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parse.Literals.Identifier.id;
import static io.zmeu.Frontend.Parse.Literals.NumberLiteral.number;
import static io.zmeu.Frontend.Parser.Expressions.AssignmentExpression.assign;
import static io.zmeu.Frontend.Parser.Statements.BlockExpression.block;
import static io.zmeu.Frontend.Parser.Statements.ExpressionStatement.expressionStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
public class WhileTest extends ParserTest {

    @Test
    void test() {
        var res = parse("""
                while (x>10) { 
                    x+=1
                }
                """);
        var expected = Program.of(
                WhileStatement.builder()
                        .test(BinaryExpression.binary(id("x"), NumberLiteral.of(10), ">"))
                        .body(expressionStatement(block(expressionStatement(assign("+=", id("x"), NumberLiteral.of(1))
                                        )
                                )
                        )).build()

        );
        log.info((res));
        assertEquals(expected, res);
    }

    @Test
    void testFor() {
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
    void testForInfinity() {
        var res = parse("""
                for (; ; ) { 
                    x+=1
                }
                """);
        var expected = Program.of(ForStatement.builder()
                .item(null)
                .body(expressionStatement(block(
                        expressionStatement(
                                assign("+=", id("x"), NumberLiteral.of(1))
                        )
                )))
                .build()
        );

        log.info((res));
        assertEquals(expected, res);
    }


}
