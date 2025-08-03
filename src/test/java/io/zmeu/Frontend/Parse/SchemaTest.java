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
import static io.kite.Frontend.Parser.Expressions.VarDeclaration.var;
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
                   var Vm x =1
                   var Vm y =1
                }
                """);
        var expected = program(
                schema(id("square"),
                        schemaProperty(var("x", type("Vm"), 1)),
                        schemaProperty(var("y", type("Vm"), 1))
                )
        );
        log.warn(actual);
        assertEquals(expected, actual);
    }

    @Test
    void schemaCloudVar() {
        var actual = (Program) parse("""
                schema square { 
                   @Cloud var Vm x =1
                }
                """);
        var expected = program(
                schema(id("square"),
                        schemaProperty(var("x", type("Vm"), 1), annotation("Cloud"))
                )
        );
        log.warn(actual);
        assertEquals(expected, actual);
    }

    @Test
    void schemaCloudWithArgsVar() {
        var actual = (Program) parse("""
                schema square { 
                   @Cloud(importable) var Vm x =1
                }
                """);
        var expected = program(
                schema(id("square"),
                        schemaProperty(var("x", type("Vm"), 1), annotation("Cloud", id("importable")))
                )
        );
        log.warn(actual);
        assertEquals(expected, actual);
    }

    @Test
    void schemaCloudArrayVar() {
        var actual = (Program) parse("""
                schema square { 
                   @Cloud([importable]) var Vm x =1
                }
                """);
        ArrayExpression array = ArrayExpression.array(id("importable"));

        var expected = program(
                schema(id("square"),
                        schemaProperty(var("x", type("Vm"), 1), annotation(id("Cloud"), array))
                )
        );
        log.warn(actual);
        assertEquals(expected, actual);
    }

    @Test
    void schemaCloudNumbersVar() {
        var actual = (Program) parse("""
                schema square { 
                   @Cloud([1,2,3]) var Vm x =1
                }
                """);
        ArrayExpression array = ArrayExpression.array(1, 2, 3);

        var expected = program(
                schema(id("square"),
                        schemaProperty(var("x", type("Vm"), 1), annotation(id("Cloud"), array))
                )
        );
        log.warn(actual);
        assertEquals(expected, actual);
    }

    @Test
    void schemaCloudStringsVar() {
        var actual = (Program) parse("""
                schema square { 
                   @Cloud(["test"]) var Vm x =1
                }
                """);
        ArrayExpression array = ArrayExpression.array("test");

        var expected = program(
                schema(id("square"),
                        schemaProperty(var("x", type("Vm"), 1), annotation(id("Cloud"), array))
                )
        );
        log.warn(actual);
        assertEquals(expected, actual);
    }

    @Test
    void schemaObjectVar() {
        var actual = (Program) parse("""
                schema square { 
                   @Cloud({env="test"}) var Vm x =1
                }
                """);
        var expected = program(
                schema(id("square"),
                        schemaProperty(var("x", type("Vm"), 1), annotation(id("Cloud"), objectExpression(object("env", "test")))
                        )
                ));
        log.warn(actual);
        assertEquals(expected, actual);
    }

    @Test
    void schemaDeclarationVar() {
        var actual = (Program) parse("""
                schema square { 
                   var Vm x =1
                }
                """);
        var expected = program(
                schema(id("square"),
                        schemaProperty(var("x", type("Vm"), 1))
                ));
        log.warn(actual);
        assertEquals(expected, actual);
    }

}
