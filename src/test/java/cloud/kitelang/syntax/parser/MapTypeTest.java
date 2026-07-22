package cloud.kitelang.syntax.parser;

import cloud.kitelang.syntax.literals.ArrayTypeIdentifier;
import cloud.kitelang.syntax.literals.MapTypeIdentifier;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.ast.Factory.program;
import static cloud.kitelang.syntax.ast.expressions.ObjectExpression.objectExpression;
import static cloud.kitelang.syntax.ast.expressions.VarDeclaration.var;
import static cloud.kitelang.syntax.ast.statements.SchemaDeclaration.schema;
import static cloud.kitelang.syntax.ast.statements.SchemaProperty.schemaProperty;
import static cloud.kitelang.syntax.ast.statements.VarStatement.varStatement;
import static cloud.kitelang.syntax.literals.Identifier.id;
import static cloud.kitelang.syntax.literals.NumberLiteral.number;
import static cloud.kitelang.syntax.literals.ObjectLiteral.object;
import static cloud.kitelang.syntax.literals.TypeIdentifier.type;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@DisplayName("Parser map type")
public class MapTypeTest extends ParserTest {

    @Test
    void mapVarEmptyInit() {
        var res = parse("var map<string, number> x = {}");
        var expected = program(varStatement(var("x", MapTypeIdentifier.mapType(type("string"), type("number")), objectExpression())));
        assertEquals(expected, res);
    }

    @Test
    void mapVarWithInit() {
        var res = parse("var map<string, number> x = { a: 1, b: 2 }");
        var expected = program(varStatement(var("x",
                MapTypeIdentifier.mapType(type("string"), type("number")),
                objectExpression(object("a", number(1)), object("b", number(2))))));
        assertEquals(expected, res);
    }

    @Test
    void mapSchemaProperty() {
        var actual = parse("""
                schema Config {
                   map<string, number> counts
                }
                """);
        var expected = program(
                schema(id("Config"),
                        schemaProperty(MapTypeIdentifier.mapType(type("string"), type("number")), "counts", null))
        );
        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("map value type may itself be a nominal/schema type")
    void mapOfNominalValueType() {
        var res = parse("var map<string, Vm> x = {}");
        var expected = program(varStatement(var("x", MapTypeIdentifier.mapType(type("string"), type("Vm")), objectExpression())));
        assertEquals(expected, res);
    }

    @Test
    @DisplayName("map value type composes with array suffix: map<string, number[]>")
    void mapOfArrayValueType() {
        var res = parse("var map<string, number[]> x = {}");
        var expected = program(varStatement(var("x",
                MapTypeIdentifier.mapType(type("string"), new ArrayTypeIdentifier(type("number"))),
                objectExpression())));
        assertEquals(expected, res);
    }

    @Test
    @DisplayName("maps nest: map<string, map<string, number>>")
    void nestedMapType() {
        var res = parse("var map<string, map<string, number>> x = {}");
        var expected = program(varStatement(var("x",
                MapTypeIdentifier.mapType(type("string"), MapTypeIdentifier.mapType(type("string"), type("number"))),
                objectExpression())));
        assertEquals(expected, res);
    }

    @Test
    @DisplayName("array suffix composes after a map type: map<string, number>[]")
    void arrayOfMapType() {
        var res = parse("var map<string, number>[] x = []");
        var expected = program(varStatement(var("x",
                new ArrayTypeIdentifier(MapTypeIdentifier.mapType(type("string"), type("number"))),
                cloud.kitelang.syntax.ast.expressions.ArrayExpression.array())));
        assertEquals(expected, res);
    }

    @Test
    @DisplayName("'map' becoming a reserved keyword must not break using it as an object property key")
    void mapAsObjectKeyStillParses() {
        var res = parse("var x = { map: \"value\" }");
        var expected = program(varStatement(var("x", objectExpression(object("map", cloud.kitelang.syntax.literals.StringLiteral.string("value"))))));
        assertEquals(expected, res);
    }
}
