package io.kite.Runtime.Decorators;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * All the tests from print but with added sensitive values.
 */
@Log4j2
public class CountTests extends DecoratorTests {

    @Test
    void outputSensitive() {
        eval("""
                @sensitive
                output string something = "a"
                """);
    }

    @Test
    void countResource() {
        eval("""
                schema vm { string name }
                
                @count(2)
                resource vm main {
                
                }
                """);
    }

    @Test
    @Disabled("not implemented")
    void countComponent() {
        eval("""
                @count(2)
                component vm main {
                
                }
                """);
    }

}
