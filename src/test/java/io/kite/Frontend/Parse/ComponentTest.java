package io.kite.Frontend.Parse;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.Frontend.Parse.Literals.NumberLiteral.number;
import static io.kite.Frontend.Parse.Literals.StringLiteral.string;
import static io.kite.Frontend.Parse.Literals.TypeIdentifier.type;
import static io.kite.Frontend.Parser.Expressions.AssignmentExpression.assign;
import static io.kite.Frontend.Parser.Expressions.ComponentStatement.component;
import static io.kite.Frontend.Parser.Expressions.InputDeclaration.input;
import static io.kite.Frontend.Parser.Expressions.OutputDeclaration.output;
import static io.kite.Frontend.Parser.Factory.program;
import static io.kite.Frontend.Parser.Statements.BlockExpression.block;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Component")
public class ComponentTest extends ParserTest {

    @Test
    void componentUnquoted() {
        var res = parse("component Backend api {}");
        var expected = program(component("Backend", "api", block()));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    @Disabled("Component type should not be a string. Use an import statement")
    void componentQuoted() {
        var res = parse("component 'Backend' api {}");
        var expected = program(component("'Backend'", "api", block()));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    @Disabled("Component type should not be a string. Use an import statement")
    void componentQuotedDouble() {
        var res = parse("""
                component "Backend" api {}
                """);
        var expected = program(component("""
                "Backend"
                """, "api", block()));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    @Disabled("Component type should not be a string. Use an import statement")
    void componentProviderNamespacedQuoted() {
        var res = parse("component 'Aws.Storage' api {}");
        var expected = program(component("'Aws.Storage'", "api", block()));
        assertEquals(expected, res);
    }

    @Test
    void componentProviderNamespacedUnquoted() {
        var res = parse("component Aws.Storage api {}");
        var expected = program(component("Aws.Storage", "api", block()));
        assertEquals(expected, res);
        log.info(res);
    }

    @Disabled("Component type should not be a string. Use an import statement")
    @Test
    void componentProviderWithResourceNamespaced() {
        var res = parse("component 'Aws.Storage/S3.Bucket' api {}");
        var expected = program(component("'Aws.Storage/S3.Bucket'", "api", block()));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    @Disabled("Component type should not be a string. Use an import statement")
    void componentProviderResourceNamespacedWithDate() {
        var res = parse("component 'Aws.Storage/S3.Bucket@2022-01-20' api {}");
        var expected = program(component("'Aws.Storage/S3.Bucket@2022-01-20'", "api", block()));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void componentWithBody() {
        var res = parse("""
                component Aws.Storage.S3.Bucket api {
                    name = "bucket-prod"
                }
                """);
        var expected = program(
                component("Aws.Storage.S3.Bucket", "api",
                        block(assign("name", "bucket-prod"))));
        assertEquals(expected, res);
        log.info(res);
    }

    @Test
    void componentInputs() {
        var res = parse("""
                component Aws.Storage.S3.Bucket {
                    input string name
                    input string size
                }
                """);
        var expected = program(component("Aws.Storage.S3.Bucket",
                block(
                        input("name", type("string")),
                        input("size", type("string"))
                )));
        assertEquals(expected, res);
    }

    @Test
    void componentInputsSimple() {
        var res = parse("""
                component Bucket {
                    input string name
                    input string size
                }
                """);
        var expected = program(component("Bucket",
                block(
                        input("name", type("string")),
                        input("size", type("string"))
                )));
        assertEquals(expected, res);
    }

    @Test
    void componentOutputSimple() {
        var res = parse("""
                component Bucket {
                    output string name = "bucket-prod"
                }
                """);
        var expected = program(component("Bucket",
                block(
                        output("name", type("string"), string("bucket-prod"))
                )));
        assertEquals(expected, res);
    }

    @Test
    void componentInputsDefaults() {
        var res = parse("""
                component Aws.Storage.S3.Bucket {
                    input string name = 'bucket-prod'
                    input number size = 10
                }
                """);
        var expected = program(component("Aws.Storage.S3.Bucket",
                block(
                        input("name", type("string"), string("bucket-prod")),
                        input("size", type("number"), number(10))
                )));
        assertEquals(expected, res);
    }

}
