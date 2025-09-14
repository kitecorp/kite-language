package io.kite.Frontend.Parse;

import io.kite.Frontend.Parser.Expressions.ArrayExpression;
import io.kite.Frontend.Parser.Program;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.Frontend.Parse.Literals.Identifier.id;
import static io.kite.Frontend.Parse.Literals.ObjectLiteral.object;
import static io.kite.Frontend.Parse.Literals.TypeIdentifier.type;
import static io.kite.Frontend.Parser.Expressions.AnnotationDeclaration.annotation;
import static io.kite.Frontend.Parser.Expressions.ObjectExpression.objectExpression;
import static io.kite.Frontend.Parser.Factory.program;
import static io.kite.Frontend.Parser.Statements.SchemaDeclaration.SchemaProperty.schemaProperty;
import static io.kite.Frontend.Parser.Statements.SchemaDeclaration.schema;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Schema")
public class SchemaTest extends ParserTest {

    @Test
    void schemaDeclaration() {
        var actual = (Program) parse("""
                schema square { 
                   Vm x =1
                   Vm y =1
                }
                """);
        var expected = program(
                schema(id("square"),
                        schemaProperty(type("Vm"),"x", 1),
                        schemaProperty(type("Vm"),"y", 1)
                )
        );
        assertEquals(expected, actual);
    }

    @Test
    void schemaCloudVar() {
        var actual = (Program) parse("""
                schema square { 
                   @cloud Vm x =1
                }
                """);
        var expected = program(
                schema(id("square"),
                        schemaProperty(type("Vm"),"x",  1, annotation("cloud"))
                )
        );
        assertEquals(expected, actual);
    }

    @Test
    void schemaCloudWithArgsVar() {
        var actual = (Program) parse("""
                schema square { 
                   @cloud(importable) Vm x =1
                }
                """);
        var expected = program(
                schema(id("square"),
                        schemaProperty(type("Vm"),"x",  1, annotation("cloud", id("importable")))
                )
        );
        assertEquals(expected, actual);
    }

    @Test
    void schemaCloudArrayVar() {
        var actual = (Program) parse("""
                schema square { 
                   @cloud([importable]) Vm x =1
                }
                """);
        ArrayExpression array = ArrayExpression.array(id("importable"));

        var expected = program(
                schema(id("square"),
                        schemaProperty(type("Vm"),"x", 1, annotation(id("cloud"), array))
                )
        );
        assertEquals(expected, actual);
    }

    @Test
    void schemaCloudNumbersVar() {
        var actual = (Program) parse("""
                schema square { 
                   @cloud([1,2,3]) Vm x =1
                }
                """);
        var array = ArrayExpression.array(1, 2, 3);

        var expected = program(
                schema(id("square"),
                        schemaProperty(type("Vm"),"x",  1, annotation(id("cloud"), array))
                )
        );
        assertEquals(expected, actual);
    }

    @Test
    void schemaCloudStringsVar() {
        var actual = (Program) parse("""
                schema square { 
                   @cloud(["test"]) Vm x =1
                }
                """);
        var array = ArrayExpression.array("test");

        var expected = program(
                schema(id("square"),
                        schemaProperty(type("Vm"),"x", 1, annotation(id("cloud"), array))
                )
        );
        assertEquals(expected, actual);
    }

    @Test
    void schemaObjectVar() {
        var actual = (Program) parse("""
                schema square { 
                   @cloud({env="test"}) Vm x =1
                }
                """);
        var expected = program(
                schema(id("square"),
                        schemaProperty(type("Vm"),"x",  1, annotation(id("cloud"),
                                objectExpression(object("env", "test")))
                        )
                ));
        assertEquals(expected, actual);
    }

    @Test
    void schemaDeclarationVar() {
        var actual = (Program) parse("""
                schema square { 
                   Vm x =1
                }
                """);
        var expected = program(
                schema(id("square"),
                        schemaProperty(type("Vm"),"x", 1))
                );
        assertEquals(expected, actual);
    }

}
