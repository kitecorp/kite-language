package io.kite.frontend.parse;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.frontend.parse.literals.Identifier.id;
import static io.kite.frontend.parse.literals.TypeIdentifier.type;
import static io.kite.frontend.parser.Factory.program;
import static io.kite.frontend.parser.statements.SchemaDeclaration.schema;
import static io.kite.frontend.parser.statements.SchemaProperty.schemaProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

}
