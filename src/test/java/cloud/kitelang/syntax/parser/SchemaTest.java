package cloud.kitelang.syntax.parser;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static cloud.kitelang.syntax.ast.Factory.program;
import static cloud.kitelang.syntax.ast.statements.SchemaDeclaration.schema;
import static cloud.kitelang.syntax.ast.statements.SchemaProperty.*;
import static cloud.kitelang.syntax.literals.Identifier.id;
import static cloud.kitelang.syntax.literals.TypeIdentifier.type;
import static org.junit.jupiter.api.Assertions.*;
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
    void schemaWithCloudAnnotation() {
        var actual = parse("""
                schema Bucket {
                   @cloud string arn
                }
                """);
        var schema = (cloud.kitelang.syntax.ast.statements.SchemaDeclaration) actual.getBody().getFirst();
        var prop = schema.getProperties().getFirst();
        assertTrue(prop.isCloudGenerated());  // @cloud marks property as cloud-generated
        assertEquals("arn", prop.name());
    }

    @Test
    void schemaWithMixedProperties() {
        var actual = parse("""
                schema Bucket {
                   string name
                   string region
                   @cloud string arn
                   @cloud string endpoint
                }
                """);
        var schema = (cloud.kitelang.syntax.ast.statements.SchemaDeclaration) actual.getBody().getFirst();
        var props = schema.getProperties();

        assertEquals(4, props.size());
        assertFalse(props.get(0).isCloudGenerated()); // string name (regular property)
        assertFalse(props.get(1).isCloudGenerated()); // string region (regular property)
        assertTrue(props.get(2).isCloudGenerated());  // @cloud string arn
        assertTrue(props.get(3).isCloudGenerated());  // @cloud string endpoint
    }

    @Test
    void schemaCloudPropertyWithInitializer() {
        var actual = parse("""
                schema Bucket {
                   string name
                   @cloud string url = "https://" + name + ".s3.amazonaws.com"
                }
                """);
        var schema = (cloud.kitelang.syntax.ast.statements.SchemaDeclaration) actual.getBody().getFirst();
        var props = schema.getProperties();

        assertFalse(props.get(0).isCloudGenerated());
        assertTrue(props.get(1).isCloudGenerated());  // @cloud marks as cloud-generated
        assertTrue(props.get(1).hasInit());
    }

}
