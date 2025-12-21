package cloud.kitelang.execution.decorators;

import cloud.kitelang.execution.values.ResourceValue;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * Maybe print the description of the fields
 */
@Slf4j
public class DescriptionTests extends DecoratorTests {

    @Test
    void dependsOnSingleResource() {
        var res = (ResourceValue) eval("""
                schema vm { string name }
                
                resource vm first { }
                
                @description("hello")
                resource vm second {
                
                }
                """);
        log.warn("{}", res);
    }


}
