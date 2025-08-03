package io.zmeu.TypeChecker;

import io.zmeu.Base.CheckerTest;
import io.zmeu.TypeChecker.Types.ResourceType;
import io.zmeu.TypeChecker.Types.ValueType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TypeChecker If")
public class IfTest extends CheckerTest {

    @Test
    void testBlock() {
        var actual = eval("""
                var x = 10
                var y = 10
                if (x<=10) {
                   y = 2
                } else {
                   y = 3
                }
                y
                """);
        assertEquals(ValueType.Number, actual);
    }

    @Test
    void arrayResourcesAssignedToVar() {
        var array = eval("""
                schema Bucket {
                   var string name
                }
                if true {
                    resource Bucket photos {
                      name     = 'name'
                    }
                }
                """);

        Assertions.assertInstanceOf(ResourceType.class, array);
        var resourceType = (ResourceType) array;
        var res = new ResourceType("photos", resourceType.getSchema(), resourceType.getEnvironment());
        assertEquals(res, resourceType);
    }

}
