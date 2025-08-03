package io.kite.Frontend.Parse;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.Frontend.Parser.Expressions.AssignmentExpression.assign;
import static io.kite.Frontend.Parser.Expressions.MemberExpression.member;
import static io.kite.Frontend.Parser.Factory.expressionStatement;
import static io.kite.Frontend.Parser.Factory.program;
import static io.kite.Frontend.Parse.Literals.StringLiteral.string;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Member Expression")
public class MemberExpressionTest extends ParserTest {

    @Test
    void testMember() {
        var res = parse("x.y");
        var expected = program(expressionStatement(member("x", "y")));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testMemberAssignment() {
        var res = parse("x.y = 1");
        var expected = program(expressionStatement(
                assign(member("x", "y"), 1, "="))
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testMemberAssignmentComputed() {
        var res = parse("x[0] = 1");
        var expected = program(expressionStatement(assign(
                member(true, "x", 0), 1, "="))
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testMemberComputedNested() {
        var res = parse("x.y.z['key']");
        var expected = program(expressionStatement(
                        member(true, member(member("x", "y"), "z"), string("key"))
                )
        );
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void testMemberComputedNestedAssignment() {
        var res = parse("x.y.z['key'] = 1");
        var expected = program(expressionStatement(
                assign("=",
                        member(true, member(member("x", "y"), "z"), string("key")),
                        1)
        ));
        assertEquals(expected, res);
        log.info((res));
    }

}
