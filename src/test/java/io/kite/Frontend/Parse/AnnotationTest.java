package io.kite.Frontend.Parse;

import io.kite.Frontend.Parser.Expressions.ResourceExpression;
import io.kite.Frontend.Parser.Factory;
import io.kite.Frontend.Parser.Program;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static io.kite.Frontend.Parse.Literals.BooleanLiteral.bool;
import static io.kite.Frontend.Parse.Literals.Identifier.symbol;
import static io.kite.Frontend.Parse.Literals.NumberLiteral.number;
import static io.kite.Frontend.Parse.Literals.StringLiteral.string;
import static io.kite.Frontend.Parse.Literals.SymbolIdentifier.id;
import static io.kite.Frontend.Parse.Literals.TypeIdentifier.type;
import static io.kite.Frontend.Parser.Expressions.AnnotationDeclaration.annotation;
import static io.kite.Frontend.Parser.Expressions.ArrayExpression.array;
import static io.kite.Frontend.Parser.Expressions.ComponentStatement.component;
import static io.kite.Frontend.Parser.Expressions.InputDeclaration.input;
import static io.kite.Frontend.Parser.Expressions.OutputDeclaration.output;
import static io.kite.Frontend.Parser.Expressions.VarDeclaration.var;
import static io.kite.Frontend.Parser.Program.program;
import static io.kite.Frontend.Parser.Statements.BlockExpression.block;
import static io.kite.Frontend.Parser.Statements.SchemaDeclaration.SchemaProperty.schemaProperty;
import static io.kite.Frontend.Parser.Statements.SchemaDeclaration.schema;
import static io.kite.Frontend.Parser.Statements.VarStatement.varStatement;

/**
 * Annotations can be attached to:
 * - Input
 * - Output
 * - Var
 * - Resource
 * - Component
 * - Schema
 * - SchemaProperty
 */
@Log4j2
@DisplayName("Parser annotations")
public class AnnotationTest extends ParserTest {

    @Test
    void annotationOutput() {
        var res = parse("""
                @annotation
                output string something = 10
                """);
        Program annotation = program(
                output(symbol("something"), type("string"), number(10), Set.of(annotation("annotation"))));
        Assertions.assertEquals(annotation, res);
    }

    @Test
    void annotationVar() {
        var res = parse("""
                @annotation
                var string something = 10
                """);
        var program = program(
                varStatement(var("something", type("string"), number(10), annotation("annotation")))
        );
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationInput() {
        var res = parse("""
                @annotation
                input string something = 10
                """);
        var program = program(
                input("something", type("string"), 10, annotation("annotation"))
        );
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationResource() {
        var res = parse("""
                @annotation
                resource vm something { }
                """);
        var program = program(
                ResourceExpression.resource(type("vm"), symbol("something"), Set.of(annotation("annotation")), block())
        );
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationComponent() {
        var res = parse("""
                @annotation
                component Backend api { }
                """);
        var program = Factory.program(component("Backend", "api", block(), annotation("annotation")));
        Assertions.assertEquals(program, res);
    }


    @Test
    void annotationSchema() {
        var res = parse("""
                @annotation
                @version
                schema Backend { string name = 0 }
                """);

        var program = Factory.program(
                schema(
                        id("Backend"),
                        List.of(schemaProperty(type("string"), "name", 0)),
                        annotation("annotation"),
                        annotation("version")
                )
        );
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationSchemaProperty() {
        var res = parse("""
                @annotation
                schema Backend { @sensitive @deprecated string name = 0 }
                """);

        var program = Factory.program(
                schema(
                        id("Backend"),
                        List.of(schemaProperty(type("string"), "name", 0, annotation("sensitive"), annotation("deprecated"))),
                        annotation("annotation")
                )
        );
        Assertions.assertEquals(program, res);
    }


    @Test
    void annotationNumber() {
        var res = parse("""
                @annotation(2)
                component Backend api { }
                """);
        var program = Factory.program(component("Backend", "api", block(), annotation("annotation", number(2))));
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationDecimal() {
        var res = parse("""
                @annotation(2.2)
                component Backend api { }
                """);
        var program = Factory.program(component("Backend", "api", block(), annotation("annotation", number(2.2))));
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationString() {
        var res = parse("""
                @annotation("2.2")
                component Backend api { }
                """);
        var program = Factory.program(component("Backend", "api", block(), annotation("annotation", string("2.2"))));
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationTrue() {
        var res = parse("""
                @annotation(true)
                component Backend api { }
                """);
        var program = Factory.program(component("Backend", "api", block(), annotation("annotation", bool(true))));
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationFalse() {
        var res = parse("""
                @annotation(false)
                component Backend api { }
                """);
        var program = Factory.program(component("Backend", "api", block(), annotation("annotation", bool(false))));
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationArray() {
        var res = parse("""
                @annotation([1,2,3])
                component Backend api { }
                """);
        var program = Factory.program(component("Backend", "api", block(),
                annotation("annotation", array(1, 2, 3))));
        Assertions.assertEquals(program, res);
    }

}
