package io.kite.Frontend.Parse;

import io.kite.Frontend.Parser.Expressions.AssignmentExpression;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.kite.Frontend.Parser.Expressions.ModuleExpression.module;
import static io.kite.Frontend.Parser.Factory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Log4j2
@DisplayName("Parser Module")
public class ModuleTest extends ParserTest {

    @Test
    void moduleUnquoted() {
        var res = parse("module Backend api {}");
        var expected = program(module("Backend", "api", block()));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void moduleQuoted() {
        var res = parse("module 'Backend' api {}");
        var expected = program(module("'Backend'", "api", block()));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void moduleQuotedDouble() {
        var res = parse("""
                module "Backend" api {}
                """);
        var expected = program(module("""
                "Backend"
                """, "api", block()));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void moduleProviderNamespacedQuoted() {
        var res = parse("module 'Aws.Storage' api {}");
        var expected = program(module("'Aws.Storage'", "api", block()));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void moduleProviderNamespacedUnquoted() {
        var res = parse("module Aws.Storage api {}");
        var expected = program(module("Aws.Storage", "api", block()));
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
    void moduleProviderWithResourceNamespaced() {
        var res = parse("module 'Aws.Storage/S3.Bucket' api {}");
        var expected = program(module("'Aws.Storage/S3.Bucket'", "api", block()));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void moduleProviderResourceNamespacedWithDate() {
        var res = parse("module 'Aws.Storage/S3.Bucket@2022-01-20' api {}");
        var expected = program(module("'Aws.Storage/S3.Bucket@2022-01-20'", "api", block()));
        assertEquals(expected, res);
        log.info((res));
    }

    @Test
    void moduleWithBody() {
        var res = parse("""
                module 'Aws.Storage/S3.Bucket@2022-01-20' api {
                    name = "bucket-prod"
                }
                """);
        var expected = program(module("'Aws.Storage/S3.Bucket@2022-01-20'", "api",
                block(AssignmentExpression.assign("name", "bucket-prod"))));
        assertEquals(expected, res);
        log.info((res));
    }


}
