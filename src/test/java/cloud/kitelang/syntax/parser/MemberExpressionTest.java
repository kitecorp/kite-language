package cloud.kitelang.syntax.parser;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.ast.Factory.expressionStatement;
import static cloud.kitelang.syntax.ast.Factory.program;
import static cloud.kitelang.syntax.ast.expressions.AssignmentExpression.assign;
import static cloud.kitelang.syntax.ast.expressions.MemberExpression.member;
import static cloud.kitelang.syntax.literals.StringLiteral.string;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DisplayName("Parser Member Expression")
public class MemberExpressionTest extends ParserTest {

    @Test
    void testMember() {
        var res = parse("x.y");
        var expected = program(expressionStatement(member("x", "y")));
        assertEquals(expected, res);
    }

    @Test
    void testMemberAssignment() {
        var res = parse("x.y = 1");
        var expected = program(expressionStatement(
                assign(member("x", "y"), 1, "="))
        );
        assertEquals(expected, res);
    }

    @Test
    void testMemberAssignmentComputed() {
        var res = parse("x[0] = 1");
        var expected = program(expressionStatement(assign(
                member(true, "x", 0), 1, "="))
        );
        assertEquals(expected, res);
    }

    @Test
    void testMemberComputedNested() {
        var res = parse("x.y.z['key']");
        var expected = program(expressionStatement(
                        member(true, member(member("x", "y"), "z"), string("key"))
                )
        );
        assertEquals(expected, res);
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
    }

}
