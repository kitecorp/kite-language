package io.zmeu.Frontend.Parse;

import io.zmeu.Frontend.Parser.Program;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parse.Literals.Identifier.id;
import static io.zmeu.Frontend.Parse.Literals.TypeIdentifier.type;
import static io.zmeu.Frontend.Parser.Factory.program;
import static io.zmeu.Frontend.Parser.Statements.SchemaDeclaration.SchemaProperty.schemaProperty;
import static io.zmeu.Frontend.Parser.Statements.SchemaDeclaration.schema;
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
                        schemaProperty("x", type("Vm"), 1),
                        schemaProperty("y", type("Vm"), 1)
                )
        );
        log.warn(actual);
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
                        schemaProperty("x", type("Vm"), 1)
                ));
        log.warn(actual);
        assertEquals(expected, actual);
    }

}
