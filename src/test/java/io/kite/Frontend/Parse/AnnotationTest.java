package io.kite.Frontend.Parse;

import io.kite.Frontend.Parse.Literals.TypeIdentifier;
import io.kite.Frontend.Parser.ParserErrors;
import io.kite.Frontend.Parser.Program;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.Frontend.Parser.Expressions.AnnotationDeclaration.annotation;
import static io.kite.Frontend.Parser.Expressions.OutputDeclaration.output;
import static io.kite.Frontend.Parser.Program.program;
import static io.kite.Frontend.Parser.Statements.ExpressionStatement.expressionStatement;

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
                expressionStatement(annotation("annotation")),
                output("something", TypeIdentifier.type("string"), 10));
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
