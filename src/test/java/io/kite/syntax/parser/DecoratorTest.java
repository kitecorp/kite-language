package io.kite.syntax.parser;

import io.kite.syntax.ast.Factory;
import io.kite.syntax.ast.Program;
import io.kite.syntax.ast.ValidationException;
import io.kite.syntax.ast.expressions.ArrayExpression;
import io.kite.syntax.ast.expressions.ResourceStatement;
import io.kite.syntax.parser.literals.Identifier;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.kite.syntax.ast.Program.program;
import static io.kite.syntax.ast.expressions.AnnotationDeclaration.annotation;
import static io.kite.syntax.ast.expressions.ArrayExpression.array;
import static io.kite.syntax.ast.expressions.ComponentStatement.component;
import static io.kite.syntax.ast.expressions.InputDeclaration.input;
import static io.kite.syntax.ast.expressions.ObjectExpression.objectExpression;
import static io.kite.syntax.ast.expressions.OutputDeclaration.output;
import static io.kite.syntax.ast.expressions.VarDeclaration.var;
import static io.kite.syntax.ast.statements.BlockExpression.block;
import static io.kite.syntax.ast.statements.SchemaDeclaration.schema;
import static io.kite.syntax.ast.statements.SchemaProperty.schemaProperty;
import static io.kite.syntax.ast.statements.VarStatement.varStatement;
import static io.kite.syntax.parser.literals.BooleanLiteral.bool;
import static io.kite.syntax.parser.literals.NumberLiteral.number;
import static io.kite.syntax.parser.literals.ObjectLiteral.object;
import static io.kite.syntax.parser.literals.StringLiteral.string;
import static io.kite.syntax.parser.literals.SymbolIdentifier.id;
import static io.kite.syntax.parser.literals.TypeIdentifier.type;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Annotations can be attached to:
 * - Input
 * - Output
 * - Var
 * - Resource
 * - Component
 * - Schema
 * - SchemaProperty
 */
@Log4j2
@DisplayName("Parser decorator")
public class DecoratorTest extends ParserTest {

    @Test
    void annotationOutput() {
        var res = parse("""
                @annotation
                output string something = 10
                """);
        Program annotation = program(
                output(Identifier.id("something"), type("string"), number(10), Set.of(annotation("annotation"))));
        Assertions.assertEquals(annotation, res);
    }

    @Test
    void annotationVar() {
        var res = parse("""
                @annotation
                var string something = 10
                """);
        var program = program(
                varStatement(var("something", type("string"), number(10), annotation("annotation")))
        );
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationInput() {
        var res = parse("""
                @annotation
                input string something = 10
                """);
        var program = program(
                input("something", type("string"), 10, annotation("annotation"))
        );
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationResource() {
        var res = parse("""
                @annotation
                resource vm something { }
                """);
        var program = program(
                ResourceStatement.resource(type("vm"), Identifier.id("something"), Set.of(annotation("annotation")), block())
        );
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationComponent() {
        var res = parse("""
                @annotation
                component Backend api { }
                """);
        var program = Factory.program(
                component("Backend", "api", block(), annotation("annotation"))
        );
        Assertions.assertEquals(program, res);
    }


    @Test
    void annotationSchema() {
        var res = parse("""
                @annotation
                @version
                schema Backend { string name = 0 }
                """);

        var version = annotation("version");
        var annotation1 = annotation("annotation");
        var program = Factory.program(
                schema(
                        id("Backend"),
                        List.of(schemaProperty(type("string"), "name", 0)),
                        annotation1,
                        version
                )
        );
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationSchemaProperty() {
        var res = parse("""
                @annotation
                schema Backend { @sensitive @deprecated string name = 0 }
                """);

        var program = Factory.program(
                schema(
                        id("Backend"),
                        List.of(schemaProperty(type("string"), "name", 0, annotation("sensitive"), annotation("deprecated"))),
                        annotation("annotation")
                )
        );
        Assertions.assertEquals(program, res);
    }


    @Test
    void annotationNumber() {
        var res = parse("""
                @annotation(2)
                component Backend api { }
                """);
        var program = Factory.program(
                component("Backend", "api", block(), annotation("annotation", number(2)))
        );
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationDecimal() {
        var res = parse("""
                @annotation(2.2)
                component Backend api { }
                """);
        var program = Factory.program(
                component("Backend", "api", block(), annotation("annotation", number(2.2)))
        );
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationString() {
        var res = parse("""
                @annotation("2.2")
                component Backend api { }
                """);
        var program = Factory.program(
                component("Backend", "api", block(), annotation("annotation", string("2.2")))
        );
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationTrue() {
        var res = parse("""
                @annotation(true)
                component Backend api { }
                """);
        var program = Factory.program(
                component("Backend", "api", block(), annotation("annotation", bool(true)))
        );
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationFalse() {
        var res = parse("""
                @annotation(false)
                component Backend api { }
                """);
        var program = Factory.program(
                component("Backend", "api", block(), annotation("annotation", bool(false)))
        );
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationArray() {
        var res = parse("""
                @annotation([1,2,3])
                component Backend api { }
                """);
        var program = Factory.program(
                component("Backend", "api", block(),
                        annotation("annotation", array(1, 2, 3))));
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationArrayEmpty() {
        var res = parse("""
                @annotation([])
                component Backend api { }
                """);
        var program = Factory.program(
                component("Backend", "api", block(),
                        annotation("annotation", array())));
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationObjectEmpty() {
        var res = parse("""
                @annotation({})
                component Backend api { }
                """);
        var program = Factory.program(
                component("Backend", "api", block(),
                        annotation("annotation", objectExpression())));
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationNamedFields() {
        var res = parse("""
                @annotation(regex="^[a-z0-9-]+$")
                component Backend api { }
                """);
        var annotation = annotation("annotation", Map.of("regex", string("^[a-z0-9-]+$")));
        var program = Factory.program(
                component("Backend", "api", block(), annotation));
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationNamedMultipleFieldsNumber() {
        var res = parse("""
                @annotation(regex="^[a-z0-9-]+$", flags = 1)
                component Backend api { }
                """);
        var annotation = annotation("annotation", Map.of("regex", string("^[a-z0-9-]+$"), "flags", number(1)));
        var program = Factory.program(
                component("Backend", "api", block(), annotation));
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationNamedMultipleFieldsString() {
        var res = parse("""
                @annotation(regex="^[a-z0-9-]+$", flags = "m")
                component Backend api { }
                """);
        var annotation = annotation("annotation", Map.of("regex", string("^[a-z0-9-]+$"), "flags", string("m")));
        var program = Factory.program(
                component("Backend", "api", block(), annotation));
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationNamedMultipleFieldsBoolean() {
        var res = parse("""
                @annotation(regex="^[a-z0-9-]+$", flags = true)
                component Backend api { }
                """);
        var annotation = annotation("annotation", Map.of("regex", string("^[a-z0-9-]+$"), "flags", bool(true)));
        var program = Factory.program(
                component("Backend", "api", block(), annotation));
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationNamedMultipleFieldsNumberArray() {
        var res = parse("""
                @annotation(regex="^[a-z0-9-]+$", flags = [1,2,3])
                component Backend api { }
                """);
        var annotation = annotation("annotation", Map.of("regex", string("^[a-z0-9-]+$"), "flags", ArrayExpression.array(number(1), number(2), number(3))));
        var program = Factory.program(
                component("Backend", "api", block(), annotation));
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationNamedMultipleFieldsNumberArrayLastComa() {
        var res = parse("""
                @annotation(
                    regex = "^[a-z0-9-]+$", 
                    flags = [1,2,3], 
                
                )
                component Backend api { }
                """);
        var annotation = annotation("annotation", Map.of("regex", string("^[a-z0-9-]+$"), "flags", ArrayExpression.array(number(1), number(2), number(3))));
        var program = Factory.program(
                component("Backend", "api", block(), annotation));
        Assertions.assertEquals(program, res);
    }

    @Test
    void annotationObject() {
        var res = parse("""
                @annotation({env: 'prod'})
                component Backend api { }
                """);
        var program = Factory.program(
                component("Backend", "api", block(),
                        annotation("annotation", objectExpression(object("env", "prod")))));
        Assertions.assertEquals(program, res);
    }

    @Test
    void schemaCloudStringsVar() {
        var actual = parse("""
                schema square { 
                   @annotation(["test"]) Vm x =1
                }
                """);
        var array = ArrayExpression.array("test");

        var expected = Factory.program(
                schema(Identifier.id("square"),
                        schemaProperty(type("Vm"), "x", 1, annotation(Identifier.id("annotation"), array))
                )
        );
        assertEquals(expected, actual);
    }

    @Test
    void schemaObjectVar() {
        var actual = parse("""
                schema square { 
                   @annotation({env: "test"}) Vm x =1
                }
                """);
        var expected = Factory.program(
                schema(Identifier.id("square"),
                        schemaProperty(type("Vm"), "x", 1,
                                annotation("annotation", objectExpression(object("env", "test")))
                        )
                ));
        assertEquals(expected, actual);
    }

    @Test
    void schemaCloudNumbersVar() {
        var actual = parse("""
                schema square { 
                   @annotation([1,2,3]) Vm x =1
                }
                """);
        var array = ArrayExpression.array(1, 2, 3);

        var expected = Factory.program(
                schema(Identifier.id("square"),
                        schemaProperty(type("Vm"), "x", 1, annotation(Identifier.id("annotation"), array))
                )
        );
        assertEquals(expected, actual);
    }

    @Test
    void schemaCloudArrayVar() {
        var actual = parse("""
                schema square { 
                   @annotation([importable]) Vm x =1
                }
                """);
        ArrayExpression array = ArrayExpression.array(Identifier.id("importable"));

        var expected = Factory.program(
                schema(Identifier.id("square"),
                        schemaProperty(type("Vm"), "x", 1, annotation(Identifier.id("annotation"), array))
                )
        );
        assertEquals(expected, actual);
    }

    @Test
    void schemaCloudVar() {
        var actual = parse("""
                schema square { 
                   @annotation Vm x =1
                }
                """);
        var expected = Factory.program(
                schema(Identifier.id("square"),
                        schemaProperty(type("Vm"), "x", 1, annotation("annotation"))
                )
        );
        assertEquals(expected, actual);
    }

    @Test
    void schemaCloudWithArgsVar() {
        var actual = parse("""
                schema square { 
                   @annotation(importable) Vm x =1
                }
                """);
        var expected = Factory.program(
                schema(Identifier.id("square"),
                        schemaProperty(type("Vm"), "x", 1, annotation("annotation", Identifier.id("importable")))
                )
        );
        assertEquals(expected, actual);
    }

    @Test
    void decoratorMissingClosingParanthesis() {
        var err = assertThrows(ValidationException.class, () -> parse("""
                schema square { 
                   @annotation(importable Vm x =1
                }
                """));
        Assertions.assertEquals("""
                Parse error at line 2:26 - mismatched input 'Vm' expecting '.', ')', '[', ','
                  @annotation(importable Vm x =1
                                         ^
                """.trim(), err.getMessage());
    }

    @Test
    void decoratorMissingClosingBracesBrackets() {
        var err = assertThrows(ValidationException.class, () ->
                parse("""
                        schema square { 
                           @annotation([importable Vm x =1
                        }
                        """)
        );
        assertEquals("""
                Parse error at line 2:27 - missing ']' to close array
                  @annotation([importable Vm x =1
                                          ^
                """.trim(), err.getMessage());
    }

    @Test
    void decoratorMissingClosingBracket() {
        var err = assertThrows(ValidationException.class, () -> parse("""
                schema square { 
                   @annotation([importable) Vm x =1
                }
                """));
        assertEquals("""
                Parse error at line 2:26 - missing ']' to close array
                  @annotation([importable) Vm x =1
                                         ^
                """.trim(), err.getMessage());
    }

    @Test
    void decoratorMissingParanthesis() {
        var err = assertThrows(ValidationException.class, () -> parse("""
                schema square { 
                   @annotation([importable] Vm x =1
                }
                """));
        assertEquals("""
                Parse error at line 2:28 - missing ')' to close decorator arguments
                  @annotation([importable] Vm x =1
                                           ^
                """.trim(), err.getMessage());
    }

    @Test
    void decoratorMissingCloseBrackets() {
        var err = assertThrows(ValidationException.class, () -> parse("""
                schema square { 
                   @annotation(importable] Vm x =1
                }
                """));
        assertEquals("""
                Parse error at line 2:25 - mismatched input ']' expecting '.', ')', '[', ','
                  @annotation(importable] Vm x =1
                                        ^
                """.trim(), err.getMessage());
    }

    @Test
    void providerMultipleArguments() {
        assertThrows(ValidationException.class, () -> eval("""
                schema vm {}
                @provider("aws", "azure")
                resource vm something {}
                """));
    }

}
