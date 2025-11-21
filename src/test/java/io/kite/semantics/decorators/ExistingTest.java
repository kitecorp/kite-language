package io.kite.semantics.Decorators;

import io.kite.base.CheckerTest;
import io.kite.semantics.TypeError;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("@existing")
public class ExistingTest extends CheckerTest {


    @Test
    void existingValidArn() {
        eval("""
                schema vm {}
                @existing("arn:aws:lambda:eu-central-1:123456789012:function:processImage")
                resource vm something {}""");
    }

    @Test
    void existingValidStringArray() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @existing(["aws", "azure"])
                resource vm something {}"""));
    }

    @Test
    void existingValidObjectArray() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @existing({"env": "prod", "cloud": "azure"})
                resource vm something {}"""));
    }

    @Test
    void existingValidObject() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @existing({
                    env: "prod", 
                    cloud: "azure"
                })
                resource vm something {}"""));
    }

    @Test
    void existing() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @existing
                resource vm something {}"""));
    }

    @Test
    void existingEmpty() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @existing()
                resource vm something {}"""));
    }

    @Test
    void existingEmptyList() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @existing([])
                resource vm something {}"""));
    }

    @Test
    void existingEmptyString() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @existing("")
                resource vm something {}"""));
    }


    @Test
    void existingEmptyStringArray() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @existing(["aws",10])
                resource vm something {}"""));
    }

    @Test
    void existingNumber() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @existing(10)
                resource vm something {}"""
        ));
    }

    @Test
    void existingInValidObject() {
        Assertions.assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @existing({ "env": 10 })
                resource vm something {}""")
        );
    }

    @Test
    void existingInValidObjectBool() {
        Assertions.assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @existing({ "env": true })
                resource vm something {}""")
        );
    }

    @Test
    void existingInValidObjectEmptyKey() {
        Assertions.assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @existing({ "": "prod" })
                resource vm something {}""")
        );
    }

    @Test
    void existingInValidObjectNestedValue() {
        Assertions.assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @existing({ "env": { "season": "prod"} })
                resource vm something {}""")
        );
    }

    @Test
    void existingInValidObjectEmptyValue() {
        Assertions.assertThrows(RuntimeException.class, () -> eval("""
                schema vm {}
                @existing({ "env": "" })
                resource vm something {}""")
        );
    }

    @Test
    void existingInValidKeyFormatMinus() {
        Assertions.assertThrows(TypeError.class, () -> eval("""
                schema vm {}
                @existing({ "env stage": "prod" })
                resource vm something {}""")
        );
    }

}
