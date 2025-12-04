package cloud.kitelang.syntax.parser;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.ast.Factory.program;
import static cloud.kitelang.syntax.ast.statements.SchemaDeclaration.schema;
import static cloud.kitelang.syntax.ast.statements.SchemaProperty.*;
import static cloud.kitelang.syntax.literals.Identifier.id;
import static cloud.kitelang.syntax.literals.TypeIdentifier.type;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Log4j2
@DisplayName("Parser Schema")
public class SchemaTest extends ParserTest {

    @Test
    void schemaDeclaration() {
        var actual = parse("""
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
    void schemaDeclarationVar() {
        var actual = parse("""
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

    @Test
    void schemaWithExplicitInput() {
        var actual = parse("""
                schema Bucket {
                   input string name
                }
                """);
        var schema = (cloud.kitelang.syntax.ast.statements.SchemaDeclaration) actual.getBody().getFirst();
        var prop = schema.getProperties().getFirst();
        assertTrue(prop.isInput());
        assertEquals("name", prop.name());
    }

    @Test
    void schemaWithOutput() {
        var actual = parse("""
                schema Bucket {
                   output string arn
                }
                """);
        var schema = (cloud.kitelang.syntax.ast.statements.SchemaDeclaration) actual.getBody().getFirst();
        var prop = schema.getProperties().getFirst();
        assertTrue(prop.isOutput());
        assertEquals("arn", prop.name());
    }

    @Test
    void schemaWithInputAndOutput() {
        var actual = parse("""
                schema Bucket {
                   input string name
                   string region
                   output string arn
                   output string endpoint
                }
                """);
        var schema = (cloud.kitelang.syntax.ast.statements.SchemaDeclaration) actual.getBody().getFirst();
        var props = schema.getProperties();

        assertEquals(4, props.size());
        assertTrue(props.get(0).isInput());   // input string name
        assertTrue(props.get(1).isRegular()); // string region (regular property)
        assertTrue(props.get(2).isOutput());  // output string arn
        assertTrue(props.get(3).isOutput());  // output string endpoint
    }

    @Test
    void schemaOutputWithInitializer() {
        var actual = parse("""
                schema Bucket {
                   input string name
                   output string url = "https://" + name + ".s3.amazonaws.com"
                }
                """);
        var schema = (cloud.kitelang.syntax.ast.statements.SchemaDeclaration) actual.getBody().getFirst();
        var props = schema.getProperties();

        assertTrue(props.get(0).isInput());
        assertTrue(props.get(1).isOutput());
        assertTrue(props.get(1).hasInit());
    }

}
