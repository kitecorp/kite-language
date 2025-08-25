package io.kite.Frontend.Parse;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.Frontend.Parse.Literals.TypeIdentifier.type;
import static io.kite.Frontend.Parser.Expressions.AssignmentExpression.assign;
import static io.kite.Frontend.Parser.Expressions.ComponentExpression.component;
import static io.kite.Frontend.Parser.Expressions.InputDeclaration.input;
import static io.kite.Frontend.Parser.Factory.program;
import static io.kite.Frontend.Parser.Statements.BlockExpression.block;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Module")
public class ComponentTest extends ParserTest {

    @Test
    void componentUnquoted() {
        var res = parse("component Backend api {}");
        var expected = program(component("Backend", "api", block()));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void componentQuoted() {
        var res = parse("component 'Backend' api {}");
        var expected = program(component("'Backend'", "api", block()));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void componentQuotedDouble() {
        var res = parse("""
                component "Backend" api {}
                """);
        var expected = program(component("""
                "Backend"
                """, "api", block()));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void componentProviderNamespacedQuoted() {
        var res = parse("component 'Aws.Storage' api {}");
        var expected = program(component("'Aws.Storage'", "api", block()));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void componentProviderNamespacedUnquoted() {
        var res = parse("component Aws.Storage api {}");
        var expected = program(component("Aws.Storage", "api", block()));
        assertEquals(expected, res);
        log.info((res));
    }

    /*
     * import S3 from 'Aws.Storage'
     *
     * resource S3.Bucket prod {
     *
     * }
     *
     * */
    @Test
    void componentProviderWithResourceNamespaced() {
        var res = parse("component 'Aws.Storage/S3.Bucket' api {}");
        var expected = program(component("'Aws.Storage/S3.Bucket'", "api", block()));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void componentProviderResourceNamespacedWithDate() {
        var res = parse("component 'Aws.Storage/S3.Bucket@2022-01-20' api {}");
        var expected = program(component("'Aws.Storage/S3.Bucket@2022-01-20'", "api", block()));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void componentWithBody() {
        var res = parse("""
                component 'Aws.Storage/S3.Bucket@2022-01-20' api {
                    name = "bucket-prod"
                }
                """);
        var expected = program(
                component("'Aws.Storage/S3.Bucket@2022-01-20'", "api",
                        block(assign("name", "bucket-prod"))));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void componentInputs() {
        var res = parse("""
                component 'Aws.Storage/S3.Bucket@2022-01-20' api {
                    input string name
                    input string size
                }
                """);
        var expected = program(component("'Aws.Storage/S3.Bucket@2022-01-20'", "api",
                block(
                        input("name", type("string")),
                        input("size", type("string"))
                )));
        assertEquals(expected, res);
    }


}
