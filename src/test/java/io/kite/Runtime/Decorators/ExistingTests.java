package io.kite.Runtime.Decorators;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Log4j2
public class ExistingTests extends DecoratorTests {

    @Test
    void existing() {
        eval("""
                schema vm {}
                @existing("arn:aws:lambda:eu-central-1:123456789012:function:processImage")
                resource vm something {}""");
        var resource = interpreter.getInstance("something");
        Assertions.assertNotNull(resource);
        Assertions.assertEquals("arn:aws:lambda:eu-central-1:123456789012:function:processImage", resource.getExisting());
    }


}
