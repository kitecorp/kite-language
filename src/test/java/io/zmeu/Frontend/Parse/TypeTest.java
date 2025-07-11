package io.zmeu.Frontend.Parse;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;

@Log4j2
@DisplayName("Parser Type")
public class TypeTest extends ParserTest {

//    @Test
//    void typeDeclaration() {
//        var res = parse("type vm = 1");
//        var expected = program(TypeExpression.type("vm", number(1)));
//        assertEquals(expected, res);
//        log.info((res));
//    }
//
//    @Test
//    void missingLeftBracketError() {
//        parse("resource vm main   }");
//        var leftErr = ErrorSystem.getErrors().get(0);
//    }
//
//    @Test
//    void resourceWithStringAssignment() {
//        var res = parse("""
//                    resource vm main {
//                        name = "main"
//                    }
//                """);
//        var expected = program(resource("vm", "main", block(
//                assign("name", "main")
//        )));
//        assertEquals(expected, res);
//        log.info((res));
//    }
//
//    @Test
//    void existingResourceWithAssignment() {
//        var res = parse("""
//                    existing resource vm main {
//                        name = "main"
//                    }
//                """);
//        var expected = program(resource(true, "vm", "main", block(
//                assign("name", "main")
//        )));
//        assertEquals(expected, res);
//        log.info((res));
//    }
//
//    @Test
//    void resourceWithNumberAssignment() {
//        var res = parse("""
//                    resource vm main {
//                        name = 1
//                    }
//                """);
//        var expected = program(resource("vm", "main", block(
//                assign("name", 1)
//        )));
//        assertEquals(expected, res);
//        log.info((res));
//    }
//
//    @Test
//    void resourceWithMemberAssignment() {
//        var res = parse("""
//                    resource vm main {
//                        name = a.b
//                    }
//                """);
//        var expected = program(resource("vm", "main", block(
//                assign("name", member("a", "b"))
//        )));
//        assertEquals(expected, res);
//        log.info((res));
//    }


}
