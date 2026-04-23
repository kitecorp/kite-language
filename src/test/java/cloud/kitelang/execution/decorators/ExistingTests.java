package cloud.kitelang.execution.decorators;

import cloud.kitelang.semantics.decorators.ExistingDecorator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
public class ExistingTests extends DecoratorTests {

    @Test
    void existingWithArn() {
        eval("""
                schema vm {}
                @existing("arn:aws:lambda:eu-central-1:123456789012:function:processImage")
                resource vm something {}""");
        var resource = interpreter.getInstance("something");
        Assertions.assertNotNull(resource);
        Assertions.assertEquals("arn:aws:lambda:eu-central-1:123456789012:function:processImage", resource.getExisting());
    }

    /**
     * No-argument form sets the query sentinel, marking this as a data source lookup.
     */
    @Test
    void existingNoArgSetsQuerySentinel() {
        eval("""
                schema vm {}
                @existing
                resource vm latest {}""");
        var resource = interpreter.getInstance("latest");
        Assertions.assertNotNull(resource);
        Assertions.assertEquals(ExistingDecorator.QUERY_SENTINEL, resource.getExisting());
        Assertions.assertTrue(resource.isExisting());
    }

    /**
     * Empty parens form is identical to the no-argument form.
     */
    @Test
    void existingEmptyParensSetsQuerySentinel() {
        eval("""
                schema vm {}
                @existing()
                resource vm latest {}""");
        var resource = interpreter.getInstance("latest");
        Assertions.assertNotNull(resource);
        Assertions.assertEquals(ExistingDecorator.QUERY_SENTINEL, resource.getExisting());
        Assertions.assertTrue(resource.isExisting());
    }

}
