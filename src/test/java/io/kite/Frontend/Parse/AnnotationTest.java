package io.kite.Frontend.Parse;

import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.Expressions.AnnotationDeclaration;
import io.kite.Frontend.Parser.ParserErrors;
import io.kite.Frontend.Parser.Program;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.kite.Frontend.Parse.Literals.Identifier.symbol;
import static io.kite.Frontend.Parse.Literals.NumberLiteral.number;
import static io.kite.Frontend.Parser.Expressions.OutputDeclaration.output;
import static io.kite.Frontend.Parser.Program.program;

@Log4j2
@DisplayName("Parser Outputs")
public class AnnotationTest extends ParserTest {

    @Test
    void annotationOutput() {
        var res = parse("""
                @annotation
                output string something = 10
                """);
        Program annotation = program(
                output(symbol("something"), TypeIdentifier.type("string"), number(10), Set.of(AnnotationDeclaration.annotation("annotation"))));
        Assertions.assertEquals(annotation, res);
    }

    @Test
    void annotationVar() {
        var res = parse("""
                @annotation
                var string something = 10
                """);
        Assertions.assertTrue(ParserErrors.hadErrors());
    }


}
