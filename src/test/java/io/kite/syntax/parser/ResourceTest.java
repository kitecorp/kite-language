package io.kite.syntax.parser;

import io.kite.syntax.ast.ValidationException;
import io.kite.syntax.ast.expressions.AnnotationDeclaration;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.syntax.ast.Factory.member;
import static io.kite.syntax.ast.Factory.program;
import static io.kite.syntax.ast.expressions.AnnotationDeclaration.annotation;
import static io.kite.syntax.ast.expressions.AssignmentExpression.assign;
import static io.kite.syntax.ast.expressions.ResourceStatement.resource;
import static io.kite.syntax.ast.statements.BlockExpression.block;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Resource")
public class ResourceTest extends ParserTest {

    @Test
    void resourceDeclaration() {
        var res = parse("resource vm main {  }");
        var expected = program(resource("vm", "main", block()));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void missingLeftBracketError() {
        var err = Assertions.assertThrows(ValidationException.class, () -> parse("resource vm main   }"));

    }

    @Test
    void resourceWithStringAssignment() {
        var res = parse("""
                    resource vm main { 
                        name = "main" 
                    }
                """);
        var expected = program(resource("vm", "main", block(
                assign("name", "main")
        )));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void existingResourceWithAssignment() {
        var res = parse("""
                    @existing
                    resource vm main { 
                        name = "main" 
                    }
                """);
        AnnotationDeclaration existing = annotation("existing");
        var expected = program(
                        resource("vm", "main", block(
                                assign("name", "main")
                        ), existing)
        );
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void resourceWithNumberAssignment() {
        var res = parse("""
                    resource vm main { 
                        name = 1
                    }
                """);
        var expected = program(resource("vm", "main", block(
                assign("name", 1)
        )));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void resourceWithMemberAssignment() {
        var res = parse("""
                    resource vm main { 
                        name = a.b
                    }
                """);
        var expected = program(resource("vm", "main", block(
                assign("name", member("a", "b"))
        )));
        assertEquals(expected, res);
        log.info(res);
    }


}
