package cloud.kitelang.execution.decorators;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
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
