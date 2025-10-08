package io.kite.TypeChecker.Decorators;

import io.kite.Base.CheckerTest;
import io.kite.TypeChecker.TypeError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("@tags")
public class TagsTest extends CheckerTest {

    @Test
    void tags() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @tags
                resource vm something {}"""));
    }

    @Test
    void tagsEmpty() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @tags()
                resource vm something {}"""));
    }

    @Test
    void tagsEmptyList() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @tags([])
                resource vm something {}"""));
    }

    @Test
    void tagsEmptyString() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @tags("")
                resource vm something {}"""));
    }

    @Test
    void tagsEmptyStringArray() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                @tags(["aws",10])
                resource vm something {}"""));
    }

    @Test
    void tagsNumber() {
        Assertions.assertThrows(TypeError.class, () ->
                eval("""
                        @tags(10)
                        resource vm something {}""")
        );
    }

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
    void tagsValidObjectMultiLine() {
        eval("""
                schema vm {}
                @tags({
                    "env": "prod", 
                    "cloud": "azure"
                })
                resource vm something {}""");
    }

}
