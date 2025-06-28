package io.zmeu.Frontend.Parse;

import io.zmeu.Frontend.Parser.Program;
import io.zmeu.Frontend.Parser.Statements.BlockExpression;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.zmeu.Frontend.Parser.Expressions.ValDeclaration.val;
import static io.zmeu.Frontend.Parser.Expressions.VarDeclaration.var;
import static io.zmeu.Frontend.Parser.Factory.program;
import static io.zmeu.Frontend.Parser.Factory.schema;
import static io.zmeu.Frontend.Parser.Literals.Identifier.id;
import static io.zmeu.Frontend.Parser.Literals.NumberLiteral.number;
import static io.zmeu.Frontend.Parser.Literals.TypeIdentifier.type;
import static io.zmeu.Frontend.Parser.Statements.ValStatement.valStatement;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Schema")
public class SchemaTest extends BaseTest {

    @Test
    void schemaDeclaration() {
        var actual = (Program) parse("""
                schema square { 
                    var Number x =1
                    val Number y =1
                }
                """);
        var expected = program(
                schema(id("square"),
                        BlockExpression.block(
                        var(id("x"), type("Number"), number(1)),
                        valStatement(val(id("y"), type("Number"), number(1))))
                ));
        log.warn(toJson(actual));
        assertEquals(toJson(expected), toJson(actual));
    }

}
