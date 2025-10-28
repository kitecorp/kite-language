package io.kite.TypeChecker.Decorators;

import io.kite.Base.CheckerTest;
import io.kite.Frontend.Parser.errors.ParseError;
import io.kite.TypeChecker.TypeError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("@tags")
public class TagsTest extends CheckerTest {


    @Test
    void tagsValidString() {
        eval("""
                schema vm {}
                @tags("aws")
                resource vm something {}""");
    }

    @Test
    void tagsValidStringArray() {
        eval("""
                schema vm {}
                @tags(["aws", "azure"])
                resource vm something {}""");
    }

    @Test
    void tagsValidObjectArray() {
        eval("""
                schema vm {}
                @tags({"env": "prod", "cloud": "azure"})
                resource vm something {}""");
    }

    @Test
    void tagsValidObject() {
        eval("""
                schema vm {}
                @tags({
                    env: "prod", 
                    cloud: "azure"
                })
                resource vm something {}""");
    }

    @Test
    void tags() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @tags
                resource vm something {}"""));
    }

    @Test
    void tagsEmpty() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @tags()
                resource vm something {}"""));
    }

    @Test
    void tagsEmptyList() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @tags([])
                resource vm something {}"""));
    }

    @Test
    void tagsEmptyString() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @tags("")
                resource vm something {}"""));
    }


    @Test
    void tagsEmptyStringArray() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @tags(["aws",10])
                resource vm something {}"""));
    }

    @Test
    void tagsNumber() {
        assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @tags(10)
                resource vm something {}"""
        ));
    }

    @Test
    void tagsInValidObject() {
        assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @tags({ "env": 10 })
                resource vm something {}""")
        );
    }

    @Test
    void tagsInValidObjectBool() {
        assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @tags({ "env": true })
                resource vm something {}""")
        );
    }

    @Test
    void tagsInValidObjectEmptyKey() {
        assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @tags({ "": "prod" })
                resource vm something {}""")
        );
    }

    @Test
    void tagsInValidObjectNestedValue() {
        assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @tags({ "env": { "season": "prod"} })
                resource vm something {}""")
        );
    }

    @Test
    void tagsInValidObjectEmptyValue() {
        assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @tags({ "env": "" })
                resource vm something {}""")
        );
    }

    @Test
    void tagsInValidKeyFormatMinus() {
        var error = assertThrows(ParseError.class, () -> eval("""
                schema vm {}
                @tags({ "env stage": "prod" })
                resource vm something {}""")
        );
        assertEquals("Invalid key format: `env stage`. Keys must be alphanumeric.", error.getMessage());
    }

}
