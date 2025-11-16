package io.kite.Frontend.Parse.Literals;

import io.kite.Frontend.Parse.ParserTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.Frontend.Parse.Literals.BooleanLiteral.bool;
import static io.kite.Frontend.Parse.Literals.Identifier.id;
import static io.kite.Frontend.Parse.Literals.NumberLiteral.number;
import static io.kite.Frontend.Parse.Literals.ObjectLiteral.object;
import static io.kite.Frontend.Parse.Literals.StringLiteral.string;
import static io.kite.Frontend.Parse.Literals.TypeIdentifier.type;
import static io.kite.Frontend.Parser.Expressions.AssignmentExpression.assign;
import static io.kite.Frontend.Parser.Expressions.MemberExpression.member;
import static io.kite.Frontend.Parser.Expressions.ObjectExpression.objectExpression;
import static io.kite.Frontend.Parser.Expressions.VarDeclaration.var;
import static io.kite.Frontend.Parser.Factory.expressionStatement;
import static io.kite.Frontend.Parser.Program.program;
import static io.kite.Frontend.Parser.Statements.VarStatement.varStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parse object var")
public class ObjectVarExpressionTest extends ParserTest {

    @Test
    void varEmptyObject() {
        var res = parse("var x = {}");
        var expected = program(varStatement(var(id("x"), objectExpression())));
        assertEquals(expected, res);
    }

    @Test
    void varInitToNumber() {
        var res = parse("var x = { a: 2}");
        var expected = program(varStatement(var(id("x"),
                objectExpression(object("a", number(2))))));
        assertEquals(expected, res);
    }

    @Test
    void varInitToDecimal() {
        var res = parse("var x = { a: 2.2}");
        var expected = program(varStatement(var(id("x"),
                objectExpression(object("a", number(2.2))))));
        assertEquals(expected, res);
    }

    @Test
    void varInitToTrue() {
        var res = parse("var x = { a:true}");
        var expected = program(varStatement(var(id("x"),
                objectExpression(object("a", bool(true))))));
        assertEquals(expected, res);
    }

    @Test
    void varInitToFalse() {
        var res = parse("var object x = { a:false}");
        var expected = program(varStatement(var("x", type("object"),
                objectExpression(object("a", bool(false)))))
        );
        assertEquals(expected, res);
    }

    @Test
    void varInitToNull() {
        var res = parse("var x = { a:null}");
        var expected = program(varStatement(var(id("x"),
                objectExpression(object("a", NullLiteral.nullLiteral())))));
        assertEquals(expected, res);
    }

    @Test
    void varInitToNesteObject() {
        var res = parse("""
                var x = { 
                    a: { 
                        b: 2
                    }
                }
                
                """);
        var expected = program(varStatement(var(id("x"),
                objectExpression(object("a",
                        objectExpression(object("b", number(2)))
                )))));
        assertEquals(expected, res);
    }

    @Test
    void varInitToString() {
        var res = parse("""
                var x = { a: "2"}
                """);
        var expected = program(varStatement(var(id("x"),
                objectExpression(object("a", string("2"))))));
        assertEquals(expected, res);
    }

    @Test
    void varInitToString2() {
        var res = parse("""
                var x = { a: "hello"}
                """);
        var expected = program(varStatement(var(id("x"),
                objectExpression(object("a", string("hello"))))));
        assertEquals(expected, res);
    }

    @Test
    void varStringInitToString() {
        var res = parse("""
                var x = { "a": "hello"}
                """);
        var expected = program(varStatement(var(id("x"),
                objectExpression(object("a", string("hello"))))));
        assertEquals(expected, res);
    }

    @Test
    void multipleProperties() {
        var res = parse("""
                var x = { 
                "a": "hello",
                "b": "hello b"
                }
                """);
        var expected = program(varStatement(var(id("x"),
                objectExpression(
                        object("a", "hello"),
                        object("b", "hello b")
                ))));
        assertEquals(expected, res);
    }

    @Test
    void multiplePropertiesMixedKeys() {
        var res = parse("""
                var x = { 
                a: "hello",
                "b": "hello b"
                }
                """);
        var expected = program(varStatement(var(id("x"),
                objectExpression(
                        object("a", "hello"),
                        object("b", "hello b")
                ))));
        assertEquals(expected, res);
    }

    @Test
    void multiplePropertiesMixedValues() {
        var res = parse("""
                var x = { 
                a: 2,
                "b": "hello b"
                }
                """);
        var expected = program(varStatement(var(id("x"),
                objectExpression(
                        object("a", number(2)),
                        object("b", string("hello b"))
                ))));
        assertEquals(expected, res);
    }

    @Test
    void multiplePropertiesRealExample() {
        var res = parse("""
                var environmentSettings = {
                   dev: {
                     name: "Development"
                   },
                   prod: {
                     name: "Production"
                   }
                 }
                
                """);
        var expected = program(
                varStatement(var("environmentSettings",
                        objectExpression(
                                object("dev",
                                        objectExpression(
                                                object("""
                                                        name
                                                        """.trim(), string("Development"))
                                        )),
                                object("prod",
                                        objectExpression(object("""
                                                name
                                                """.trim(), string("Production"))
                                        )))))
        );
        assertEquals(expected, res);
    }

    @Test
    void multiplePropertiesRealExampleNoComma() {
        var res = parse("""
                var environmentSettings = {
                   dev: {
                     name: "Development"
                     team: "backend"
                   }
                   prod: {
                     name: "Production"
                   }
                 }
                
                """);
        var expected = program(
                varStatement(var("environmentSettings",
                        objectExpression(
                                object("dev",
                                        objectExpression(
                                                object("""
                                                        name
                                                        """.trim(), string("Development")),
                                                object("""
                                                        team
                                                        """.trim(), string("backend"))
                                        )),
                                object("prod",
                                        objectExpression(object("""
                                                name
                                                """.trim(), string("Production"))
                                        )))))
        );
        assertEquals(expected, res);
    }

    @Test
    void multiplePropertiesRealExampleComma() {
        var res = parse("""
                var environmentSettings = {
                   dev: {
                     name: "Development",
                     team: "backend"
                   }
                   prod: {
                     name: "Production"
                   }
                 }
                
                """);
        var expected = program(
                varStatement(var("environmentSettings",
                        objectExpression(
                                object("dev",
                                        objectExpression(
                                                object("""
                                                        name
                                                        """.trim(), string("Development")),
                                                object("""
                                                        team
                                                        """.trim(), string("backend"))
                                        )),
                                object("prod",
                                        objectExpression(object("""
                                                name
                                                """.trim(), string("Production"))
                                        )))))
        );
        assertEquals(expected, res);
    }

    @Test
    void multiplePropertiesRealExampleCommaObject() {
        var res = parse("""
                var environmentSettings = {
                   dev: {
                     name: "Development",
                     team: "backend"
                   },
                   prod: {
                     name: "Production"
                   }
                 }
                
                """);
        var expected = program(
                varStatement(var("environmentSettings",
                        objectExpression(
                                object("dev",
                                        objectExpression(
                                                object("""
                                                        name
                                                        """.trim(), string("Development")),
                                                object("""
                                                        team
                                                        """.trim(), string("backend"))
                                        )),
                                object("prod",
                                        objectExpression(object("""
                                                name
                                                """.trim(), string("Production"))
                                        )))))
        );
        assertEquals(expected, res);
    }


    @Test
    void multiplePropertiesRealExampleNoCommaProperty() {
        var res = parse("""
                var environmentSettings = {
                   dev: {
                     name: "Development"
                     team: "backend"
                   },
                   prod: {
                     name: "Production"
                   }
                 }
                
                """);
        var expected = program(
                varStatement(var("environmentSettings",
                        objectExpression(
                                object("dev",
                                        objectExpression(
                                                object("""
                                                        name
                                                        """.trim(), string("Development")),
                                                object("""
                                                        team
                                                        """.trim(), string("backend"))
                                        )),
                                object("prod",
                                        objectExpression(object("""
                                                name
                                                """.trim(), string("Production"))
                                        )))))
        );
        assertEquals(expected, res);
    }

    @Test
    void testMember() {
        var res = parse("""
                var x = {
                    name: "backend"
                }
                x.name
                """);
        var expected = program(
                varStatement(var("x", objectExpression(object("name", string("backend"))))),
                expressionStatement(member("x", "name"))
        );
        assertEquals(expected, res);
    }

    @Test
    void testMemberAssignmentComputed() {
        var res = parse("""
                var x = {
                    name: "backend"
                }
                x["name"] = 1""");
        var member = member(true, "x", string("name"));
        var expected = program(
                varStatement(var("x", objectExpression(object("name", string("backend"))))),
                expressionStatement(assign(member, 1, "="))
        );
        assertEquals(expected, res);
    }

    @Test
    void testMemberAssignmentComputedSingleQuote() {
        var res = parse("""
                var x = {
                    name: "backend"
                }
                x['name'] = 1""");
        var member = member(true, "x", string("name"));
        var expected = program(
                varStatement(var("x", objectExpression(object("name", string("backend"))))),
                expressionStatement(assign(member, 1, "="))
        );
        assertEquals(expected, res);
    }

}
