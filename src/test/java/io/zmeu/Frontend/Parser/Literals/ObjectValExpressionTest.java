package io.zmeu.Frontend.Parser.Literals;

import io.zmeu.Frontend.Parse.ParserTest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parser.Expressions.ObjectExpression.objectExpression;
import static io.zmeu.Frontend.Parser.Expressions.ValDeclaration.val;
import static io.zmeu.Frontend.Parser.Literals.BooleanLiteral.bool;
import static io.zmeu.Frontend.Parser.Literals.Identifier.id;
import static io.zmeu.Frontend.Parser.Literals.NumberLiteral.number;
import static io.zmeu.Frontend.Parser.Literals.ObjectLiteral.object;
import static io.zmeu.Frontend.Parser.Literals.StringLiteral.string;
import static io.zmeu.Frontend.Parser.Program.program;
import static io.zmeu.Frontend.Parser.Statements.ValStatement.valStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parse object val")
public class ObjectValExpressionTest extends ParserTest {

    @Test
    void varEmptyObject() {
        var res = parse("val x = {}");
        var expected = program(valStatement(val(id("x"), objectExpression())));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToNumber() {
        var res = parse("val x = { a: 2}");
        var expected = program(valStatement(val(id("x"),
                objectExpression(object(id("a"), number(2))))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToDecimal() {
        var res = parse("val x = { a: 2.2}");
        var expected = program(valStatement(val(id("x"),
                objectExpression(object(id("a"), number(2.2))))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToTrue() {
        var res = parse("val x = { a:true}");
        var expected = program(valStatement(val(id("x"),
                objectExpression(object(id("a"), bool(true))))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToFalse() {
        var res = parse("val x = { a:false}");
        var expected = program(valStatement(val(id("x"),
                objectExpression(object(id("a"), bool(false))))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToNull() {
        var res = parse("val x = { a:null}");
        var expected = program(valStatement(val(id("x"),
                objectExpression(object(id("a"), NullLiteral.nullLiteral())))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToNesteObject() {
        var res = parse("""
                val x = { 
                    a: { 
                        b: 2
                    }
                }
                
                """);
        var expected = program(valStatement(val(id("x"),
                objectExpression(object(id("a"),
                        objectExpression(object(id("b"), number(2)))
                )))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToString() {
        var res = parse("""
                val x = { a: "2"}
                """);
        var expected = program(valStatement(val(id("x"),
                objectExpression(object(id("a"), string("2"))))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varInitToString2() {
        var res = parse("""
                val x = { a: "hello"}
                """);
        var expected = program(valStatement(val(id("x"),
                objectExpression(object(id("a"), string("hello"))))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void varStringInitToString() {
        var res = parse("""
                val x = { "a": "hello"}
                """);
        var expected = program(valStatement(val(id("x"),
                objectExpression(object(id("""
                        "a"
                        """.trim()), string("hello"))))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void multipleProperties() {
        var res = parse("""
                val x = { 
                "a": "hello",
                "b": "hello b"
                }
                """);
        var expected = program(valStatement(val(id("x"),
                objectExpression(
                        object(id("""
                                "a"
                                """.trim()), string("hello")),
                        object(id("""
                                "b"
                                """.trim()), string("hello b"))
                ))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void multiplePropertiesMixedKeys() {
        var res = parse("""
                val x = { 
                a: "hello",
                "b": "hello b"
                }
                """);
        var expected = program(valStatement(val(id("x"),
                objectExpression(
                        object(id("""
                                a
                                """.trim()), string("hello")),
                        object(id("""
                                "b"
                                """.trim()), string("hello b"))
                ))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void multiplePropertiesMixedValues() {
        var res = parse("""
                val x = { 
                a: 2,
                "b": "hello b"
                }
                """);
        var expected = program(valStatement(val(id("x"),
                objectExpression(
                        object(id("""
                                a
                                """.trim()), number(2)),
                        object(id("""
                                "b"
                                """.trim()), string("hello b"))
                ))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void multiplePropertiesRealExample() {
        var res = parse("""
                val environmentSettings = {
                   dev: {
                     name: "Development"
                   },
                   prod: {
                     name: "Production"
                   }
                 }
                
                """);
        var expected = program(
                valStatement(val("environmentSettings",
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
        log.info((res));
    }

    @Test
    void multiplePropertiesRealExampleNoComma() {
        var res = parse("""
                val environmentSettings = {
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
                valStatement(val("environmentSettings",
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
        log.info((res));
    }

    @Test
    void multiplePropertiesRealExampleComma() {
        var res = parse("""
                val environmentSettings = {
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
                valStatement(val("environmentSettings",
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
        log.info((res));
    }

    @Test
    void multiplePropertiesRealExampleCommaObject() {
        var res = parse("""
                val environmentSettings = {
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
                valStatement(val("environmentSettings",
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
        log.info((res));
    }


    @Test
    void multiplePropertiesRealExampleNoCommaProperty() {
        var res = parse("""
                val environmentSettings = {
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
                valStatement(val("environmentSettings",
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
        log.info((res));
    }


}
